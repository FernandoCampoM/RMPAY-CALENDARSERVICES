package com.retailmanager.rmpaydashboard.services.services.ProductService;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.http.HttpStatus;
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
    @Qualifier("mapperbase")
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
                    .findOneByCodeOrBarcode(prmProduct.getCode(), prmProduct.getCode());
            if (optionalProduct.isPresent()) {
                EntidadYaExisteException objException = new EntidadYaExisteException("El producto con productCode  "
                        + prmProduct.getCode() + " ya existe en la base de datos");
                throw objException;
            }
        }
        if (prmProduct.getBarcode() != null) {
            Optional<Product> optionalProduct = this.serviceDBProducts
                    .findOneByCodeOrBarcode(prmProduct.getBarcode(), prmProduct.getBarcode());
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
        if(objProduct!=null){
            objProduct = this.serviceDBProducts.save(objProduct);
        }
        
        ResponseEntity<?> rta = null;
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
            optionalProduct= this.serviceDBProducts.findOneByCodeOrBarcode(prmProduct.getCode(), prmProduct.getBarcode());
        }else{
            optionalProduct= this.serviceDBProducts.findOneByCodeOrBarcode(prmProduct.getCode(), prmProduct.getCode());
        }
         
        if (!optionalProduct.isPresent()) {
            EntidadNoExisteException objException = new EntidadNoExisteException(
                    "El producto con productCode o barCode " + prmProduct.getCode()
                            + " no existe en la base de datos");
            throw objException;
        }
        Product objProduct = optionalProduct.get();
        objProduct.setBarcode(prmProduct.getBarcode());
        //objProduct.setCategory(prmProduct.getCategory());
        objProduct.setCost(prmProduct.getCost());
        objProduct.setInventoryLevel(prmProduct.getInventoryLevel());
        objProduct.setMinimumLevel(prmProduct.getMinimumLevel());
        objProduct.setMaximumLevel(prmProduct.getMaximumLevel());
        objProduct.setName(prmProduct.getName());
        objProduct = this.serviceDBProducts.save(objProduct);
        ResponseEntity<?> rta = null;
        if (objProduct != null) {
            // if (objProduct.getObjCustomer() != null) {
            //     costumerId=objProduct.getObjCustomer().getCustomerId();
            // }
            ProductDTO objProductRta = this.mapperBase.map(objProduct, ProductDTO.class);
            // objProductRta.setObjCustomer(null);
            // objProductRta.setCostumerId(costumerId);
            rta = new ResponseEntity<ProductDTO>(objProductRta, HttpStatus.OK);
        } 
        return rta;

    }
    @Transactional
    @Override
    public ResponseEntity<?> delete(Long productId) {
        if(productId==null){
            return new ResponseEntity<Boolean>(false, HttpStatus.OK);
        }
        Optional<Product> optionalProduct = this.serviceDBProducts.findById(productId);
        if (!optionalProduct.isPresent()) {
            EntidadNoExisteException objException = new EntidadNoExisteException(
                    "El producto con productId " + productId + " no existe en la base de datos");
            throw objException;
        }
        Product objProduct = optionalProduct.get();
        if(objProduct!=null){
            this.serviceDBProducts.delete(objProduct);
        }
        
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }

    @Transactional()
    @Override
    public ResponseEntity<?> findById(Long productId) {
        ResponseEntity<?> rta = null;
        Optional<Product> optionalProduct=null;
        if(productId!=null){
            optionalProduct = this.serviceDBProducts.findById(productId);
            if (!optionalProduct.isPresent()) {
                EntidadNoExisteException objException = new EntidadNoExisteException(
                        "El producto con productId " + productId + " no existe en la base de datos");
                throw objException;
            }
        }
        // if (optionalProduct.get().getObjCustomer() != null) {
        //     costumerId=optionalProduct.get().getObjCustomer().getCustomerId();
        // }
        if(optionalProduct!=null){
            
            ProductDTO objProductDTO = this.mapperBase.map(optionalProduct.get(), ProductDTO.class);
            // objProductDTO.setObjCustomer(null);
            // objProductDTO.setCostumerId(costumerId);
            rta = new ResponseEntity<ProductDTO>(objProductDTO, HttpStatus.OK);
        }
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
                        .findOneByCodeOrBarcode(productDTO.getCode(), productDTO.getCode());
                if (optionalProduct.isPresent()) {
                    continue;
                }
            }
            if (productDTO.getBarcode() != null) {
                Optional<Product> optionalProduct = this.serviceDBProducts
                        .findOneByCodeOrBarcode(productDTO.getBarcode(), productDTO.getBarcode());
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
    
    

}
