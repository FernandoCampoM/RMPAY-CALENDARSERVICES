package com.retailmanager.rmpaydashboard.services.services.ProductService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Product;
import com.retailmanager.rmpaydashboard.repositories.ProductRepository;
import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;



/* Implementa la interface IProductService. Ver documentación en la declaración de la interface */
@Service
public class ProductService implements IProductService {
    @Autowired
    @Qualifier("mapperSalesPoint")
    private ModelMapper mapperBase;
    @Autowired
    private ProductRepository serviceDBProducts;
    //@Autowired
    //private CustomerRepository serviceDBCostumer;
    /** 
     * @param prmProduct
     * @return ResponseEntity<?>
     */
    @Transactional
    @Override
    public ResponseEntity<?> save(ProductDTO prmProduct) {
        if (prmProduct.getCode() != null) {
            
            Optional<Product> optionalProduct = this.serviceDBProducts
                    .findOneByproductCodeOrBarcode(prmProduct.getCode(), prmProduct.getCode());
            if (optionalProduct.isPresent()) {
                EntidadYaExisteException objException = new EntidadYaExisteException("El producto con productCode  "
                        + prmProduct.getCode() + " ya existe en la base de datos");
                throw objException;
            }
        }
        if (prmProduct.getBarcode() != null) {
            Optional<Product> optionalProduct = this.serviceDBProducts
                    .findOneByproductCodeOrBarcode(prmProduct.getBarcode(), prmProduct.getBarcode());
            if (optionalProduct.isPresent()) {
                EntidadYaExisteException objException = new EntidadYaExisteException(
                        "El producto con barCode  " + prmProduct.getBarcode() + " ya existe en la base de datos");
                throw objException;
            }
        }
        Product objProduct = this.mapperBase.map(prmProduct, Product.class);
        // Optional<Customer> optionalCustomer = null;
        // if (prmProduct.getCostumerId() != null) {
        //     optionalCustomer = this.serviceDBCostumer.findById(prmProduct.getCostumerId());
        //     if (!optionalCustomer.isPresent()) {
        //         EntidadNoExisteException objException = new EntidadNoExisteException(
        //                 "El customer con id " + prmProduct.getCostumerId() + " no existe en la base de datos");
        //         throw objException;
        //     }
        //     objProduct.setObjCustomer(optionalCustomer.get());
        // }
        objProduct = this.serviceDBProducts.save(objProduct);
        ResponseEntity<?> rta = null;
        String costumerId=null;
        if (objProduct != null) {
            // if (objProduct.getObjCustomer() != null) {
            //     costumerId=objProduct.getObjCustomer().getCustomerId();
            //     ///objProduct.setObjCustomer(null);
            // }
            ProductDTO objProductRta = this.mapperBase.map(objProduct, ProductDTO.class);
            // objProductRta.setObjCustomer(null);
            // objProductRta.setCostumerId(costumerId);
            rta = new ResponseEntity<ProductDTO>(objProductRta, HttpStatus.CREATED);
        } else {
            rta = new ResponseEntity<String>("Ocurrió un error inesperado al crear el producto",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;
    }
    @Transactional
    @Override
    public ResponseEntity<?> update(ProductDTO prmProduct) {
        Optional<Product> optionalProduct;
        if(prmProduct.getBarcode()!=null){
            optionalProduct= this.serviceDBProducts.findOneByproductCodeOrBarcode(prmProduct.getCode(), prmProduct.getBarcode());
        }else{
            optionalProduct= this.serviceDBProducts.findOneByproductCodeOrBarcode(prmProduct.getCode(), prmProduct.getCode());
        }
         
        if (!optionalProduct.isPresent()) {
            EntidadNoExisteException objException = new EntidadNoExisteException(
                    "El producto con productCode o barCode " + prmProduct.getCode()
                            + " no existe en la base de datos");
            throw objException;
        }
        Product objProduct = optionalProduct.get();
        objProduct.setBarcode(prmProduct.getBarcode());
        objProduct.setCategory(prmProduct.getCategory());
        objProduct.setCost(prmProduct.getCost());
        objProduct.setInventoryLevel(prmProduct.getInventoryLevel());
        objProduct.setMinimumLevel(prmProduct.getMinimumLevel());
        objProduct.setMaximumLevel(prmProduct.getMaximumLevel());
        objProduct.setName(prmProduct.getName());
        objProduct = this.serviceDBProducts.save(objProduct);
        ResponseEntity<?> rta = null;
        String costumerId=null;
        if (objProduct != null) {
            // if (objProduct.getObjCustomer() != null) {
            //     costumerId=objProduct.getObjCustomer().getCustomerId();
            // }
            ProductDTO objProductRta = this.mapperBase.map(objProduct, ProductDTO.class);
            // objProductRta.setObjCustomer(null);
            // objProductRta.setCostumerId(costumerId);
            rta = new ResponseEntity<ProductDTO>(objProductRta, HttpStatus.OK);
        } else {
            rta = new ResponseEntity<String>("Ocurrió un error inesperado al actualizar el producto",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rta;

    }
    @Transactional
    @Override
    public ResponseEntity<?> delete(Long productId) {
        Optional<Product> optionalProduct = this.serviceDBProducts.findById(productId);
        if (!optionalProduct.isPresent()) {
            EntidadNoExisteException objException = new EntidadNoExisteException(
                    "El producto con productId " + productId + " no existe en la base de datos");
            throw objException;
        }
        this.serviceDBProducts.delete(optionalProduct.get());
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }

    @Transactional()
    @Override
    public ResponseEntity<?> findById(Long productId) {
        ResponseEntity<?> rta = null;
        Optional<Product> optionalProduct = this.serviceDBProducts.findById(productId);
        if (!optionalProduct.isPresent()) {
            EntidadNoExisteException objException = new EntidadNoExisteException(
                    "El producto con productId " + productId + " no existe en la base de datos");
            throw objException;
        }
        String costumerId=null;
        // if (optionalProduct.get().getObjCustomer() != null) {
        //     costumerId=optionalProduct.get().getObjCustomer().getCustomerId();
        // }
        ProductDTO objProductDTO = this.mapperBase.map(optionalProduct.get(), ProductDTO.class);
        // objProductDTO.setObjCustomer(null);
        // objProductDTO.setCostumerId(costumerId);
        rta = new ResponseEntity<ProductDTO>(objProductDTO, HttpStatus.OK);
        return rta;
    }

    // @Transactional()
    // @Override
    // public ResponseEntity<?> findByCustomerId(String costumerId, String filter, Pageable pageable) {
    //     ResponseEntity<?> rta = null;
    //     Optional<Customer> optionalCustomer = this.serviceDBCostumer.findById(costumerId);
    //     Page<Product> result = new PageImpl<>(new ArrayList<>());
    //     if (optionalCustomer.isPresent()) {
    //         if(filter==null){
    //             result = this.serviceDBProducts.findByobjCustomerIs(optionalCustomer.get(), pageable);
    //         }else{
    //             filter = "%" + filter + "%";
    //             result = this.serviceDBProducts.findProductsByFilterAndOnlyCustomerId(costumerId, filter, pageable);
    //         }
    //     }
    //     Page<ProductDTO> resultDTO = result.map(product -> ProductDTO.tOProduct(product));
    //     rta = new ResponseEntity<>(resultDTO, HttpStatus.OK);
    //     return rta;
    // }

    // @Transactional()
    // @Override
    // public ResponseEntity<?> findAllAndFilterCustomerId(String costumerId, String filter, Pageable pageable) {
    //     ResponseEntity<?> rta = null;
    //     //Si el costumerId es Null retorna los productos pertenecientes a RetailManager
    //     if (costumerId == null && filter == null) {
    //         Page<Product> result = this.serviceDBProducts.findByobjCustomerIsNull(pageable);
    //         rta = new ResponseEntity<>(result, HttpStatus.OK);
    //     } else if (filter == null && costumerId != null) {
    //         Optional<Customer> optionalCustomer = this.serviceDBCostumer.findById(costumerId);
    //         Page<Product> result = new PageImpl<>(new ArrayList<>());
    //         if (optionalCustomer.isPresent()) {
    //             result = this.serviceDBProducts.findByobjCustomerIsOrObjCustomerIsNull(optionalCustomer.get(),
    //                     pageable);
    //         }
    //         Page<ProductDTO> resultDTO = result.map(product -> ProductDTO.tOProduct(product));
    //         rta = new ResponseEntity<>(resultDTO, HttpStatus.OK);
    //     } else if (filter != null && costumerId != null) {
    //         Optional<Customer> optionalCustomer = this.serviceDBCostumer.findById(costumerId);
    //         Page<Product> result = new PageImpl<>(new ArrayList<>());
    //         if (optionalCustomer.isPresent()) {
    //             filter = "%" + filter + "%";
    //             result = this.serviceDBProducts.findProductsByFilterAndCustomerId(costumerId, filter, pageable);
    //         }
    //         Page<ProductDTO> resultDTO = result.map(product -> ProductDTO.tOProduct(product));
    //         rta = new ResponseEntity<>(resultDTO, HttpStatus.OK);
    //     } else if (filter != null && costumerId == null) {
    //         Page<Product> result = new PageImpl<>(new ArrayList<>());

    //         filter = "%" + filter + "%";
    //         result = this.serviceDBProducts.findProductsByFilter(filter, pageable);
    //         Page<ProductDTO> resultDTO = result.map(product -> ProductDTO.tOProduct(product));
    //         rta = new ResponseEntity<>(resultDTO, HttpStatus.OK);
    //     }
    //     return rta;
    // }
    @Transactional()
    @Override
    public ResponseEntity<?> save(String costumerId, List<ProductDTO> listProductsDTO) {
        List<ProductDTO> listProductsRTA=new ArrayList<>();
        //Optional<Customer> optionalCustomer = null;
        // if(costumerId!=null){
        //     optionalCustomer = this.serviceDBCostumer.findById(costumerId);
        // }
        for (ProductDTO productDTO : listProductsDTO) {
            if (productDTO.getCode() != null) {
                Optional<Product> optionalProduct = this.serviceDBProducts
                        .findOneByproductCodeOrBarcode(productDTO.getCode(), productDTO.getCode());
                if (optionalProduct.isPresent()) {
                    continue;
                }
            }
            if (productDTO.getBarcode() != null) {
                Optional<Product> optionalProduct = this.serviceDBProducts
                        .findOneByproductCodeOrBarcode(productDTO.getBarcode(), productDTO.getBarcode());
                if (optionalProduct.isPresent()) {
                    continue;
                }
            }
            Product objProduct = this.mapperBase.map(productDTO, Product.class);
            // if (optionalCustomer!=null) {
            //     if(optionalCustomer.isPresent()){
            //         objProduct.setObjCustomer(optionalCustomer.get());
            //     }
            // }
            if(objProduct!=null){
                objProduct = this.serviceDBProducts.save(objProduct);
            }
            
            if (objProduct != null) {
                ProductDTO objProductRta = this.mapperBase.map(objProduct, ProductDTO.class);
                // objProductRta.setObjCustomer(null);
                // objProductRta.setCostumerId(costumerId);
                listProductsRTA.add(objProductRta);
            }
        }
        return new ResponseEntity<>(listProductsRTA, HttpStatus.CREATED);
    }
    // @Transactional
    // @Override
    // public ResponseEntity<?> findByCustomerIdCSV(String costumerId) {
    //     List<Product> productos=new ArrayList<>();
    //     if(costumerId.compareTo("retailmanager")==0){
    //         productos=this.serviceDBProducts.findAllProductsRetailManager();
    //     }else{
    //         boolean existe = this.serviceDBCostumer.existsById(costumerId);
    //         if(!existe){
    //             EntidadNoExisteException objException = new EntidadNoExisteException(
    //                     "El Costumer con costumerId " + costumerId + " no existe en la base de datos");
    //             throw objException;
    //         }
    //         productos=this.serviceDBProducts.findAllProductsByCustomerId(costumerId);
    //     }
    //     String encabezado="Código del Producto,"+"Código de barras,"+"Nombre,"+"Descripcion,"
    //     +"Proveedor,"+"Costo,"+"Precio de venta,"+"Balance Inicial,"+"Nivel Minimo,"+"Nivel Maximo,"
    //     +"Departamento,"+"Categoria,"+"Tax 1 (Estatal)(0-NO)(1-SI),"+"Tax 2(Municipal)(0-NO)(1-SI),"
    //     +"Es comida(0-NO)(1-SI),"+"Precio Escala-1,"+"Precio Escala-2,"+"Precio Escala-3,"
    //     +"El producto se puede ver en pantalla(0-NO)(1-SI)\n";
    //     String archCSV = System.getProperty("user.dir") + "/products.csv";
    //     try {
    //         File statText = new File(archCSV);
    //         FileOutputStream is = new FileOutputStream(statText);
    //         OutputStreamWriter osw = new OutputStreamWriter(is);
    //         Writer w = new BufferedWriter(osw);
    //         w.write(encabezado);
    //         if(productos!=null){
    //             for (Product objProduct : productos) {
    //                 w.write(createLine(objProduct));
    //             }
    //         }
    //         w.close();
    //         File file = new File(archCSV);
    //         InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
    //         return ResponseEntity.ok()
    //                 .contentLength(file.length())
    //                 .contentType(MediaType.parseMediaTypes("text/csv").get(0))
    //                 .body(resource);
    //     } catch (Exception e) {
    //         System.out.println("Error: " + e.getMessage());
    //     }
    //     return null;
    // }
    // private String createLine(Product objProduct) {
    //     String line="";
    //     line=objProduct.getProductcode()+","+nullToZero(objProduct.getBarCode())+","+nullToZero(objProduct.getProductName())+","+
    //     nullToZero(objProduct.getDescription())+","+nullToZero(objProduct.getProvider())+","+nullToZero(objProduct.getCoast())+","+nullToZero(objProduct.getRetailPrice())+","+
    //     nullToZero(objProduct.getInitialBalance())+","+nullToZero(objProduct.getMinimunLevel())+","+nullToZero(objProduct.getMaximumLevel())+","+
    //     nullToZero(objProduct.getDepartment())+","+nullToZero(objProduct.getCategory())+","+nullToZero(objProduct.getTax1())+","+nullToZero(objProduct.getTax2())+","+
    //     nullToZero(objProduct.getFood())+","+nullToZero(objProduct.getScalePrice1())+","+nullToZero(objProduct.getScalePrice2())+","+nullToZero(objProduct.getScalePrice3())+","+
    //     nullToZero(objProduct.getGraphicScreenProduct())+"\n";
    //     return line;
    // }
    private String nullToZero(Object prmObject){
        if(prmObject==null)
            return "0";
        else
            return prmObject.toString();
    }
    
    

}
