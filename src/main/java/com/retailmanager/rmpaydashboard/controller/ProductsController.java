package com.retailmanager.rmpaydashboard.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;
import com.retailmanager.rmpaydashboard.services.services.ProductService.IProductService;

@RestController
@RequestMapping("/api")
@Validated
public class ProductsController {
     /*Atributo para el servicio de persistencia */
    @Autowired
    private IProductService productService;
    /**
     * Permite crear un nuevo producto
     * @param prmProduct Nuevo Producto
     * @return objeto del Producto creado
     */
    @PostMapping("/products")
    public ResponseEntity<?> save(@Valid @RequestBody ProductDTO prmProduct){
        return productService.save(prmProduct);
    }
    /**
     * Permite CREAR una lista de Productos Pertenecientes a una cadena
     * @param prmProduct Lista de nuevos productos
     * @return Lista de productos creados
     */
    @PostMapping("/products/{costumerId}")
    public ResponseEntity<?> saveList(@PathVariable(required = false) String costumerId, @Valid @RequestBody List<ProductDTO> listProductsDTO){
        return productService.save(costumerId,listProductsDTO);
    }
    /**
     * Permite CREAR una lista de Productos que no pertenecen a ninguna cadena
     * @param prmProduct Lista de nuevos productos
     * @return Lista de productos creados
     */
    @PostMapping("/products/retailmanager")
    public ResponseEntity<?> saveList(@Valid @RequestBody List<ProductDTO> listProductsDTO){
        return productService.save(null,listProductsDTO);
    }
    /**
     * Actualiza la información de un producto ya existente
     * @param prmProduct Objeto de Producto
     * @param productCode Identificador del producto
     * @return Objeto del producto actualizado
     */
    @PutMapping("/products/{productCode}")
    public ResponseEntity<?> update(@Valid @RequestBody ProductDTO prmProduct,@PathVariable String productCode){
        prmProduct.setCode(productCode);
        return productService.update(prmProduct);
    }
    /**
     * Retorna todos los productos usando paginación con una cantidad de items por pagina por defecto de 200.
     * Además se pueden dos filtros opcionales sobre los productos. El identificador de la cadena o un filtro sobre los atributos del producto
     * @param pageable Objeto para paginación
     * @param costumerId identificador de una cadena. Opcional
     * @param filter Filtro sobre los atriutos del producto. Opcional
     * @return 
     */
    // @GetMapping("/products")
    // public ResponseEntity<?> findAll(@PageableDefault(size = 200,page = 0) Pageable pageable,@RequestParam(required=false) String costumerId,@RequestParam(required=false) String filter){
    //     return productService.findAllAndFilterCustomerId(costumerId, filter, pageable);
    // }
    /**
     * Consulta un producto por su codigo
     * @param productCode Codigo del producto
     * @return Objeto del producto
     */
    @GetMapping("/products/{productCode}")
    public ResponseEntity<?> findById(@PathVariable Long productId){
        return productService.findById(productId);
    }
    /**
     * Elimina un producto
     * @param productCode Codigo del producto
     * @return True si es exitoso. False de lo contrario
     */
    @DeleteMapping("/products/{productCode}")
    public ResponseEntity<?> delete(@PathVariable Long productId){
        return productService.delete(productId);
    }
    /**
     * Consulta los producto de una cadena.
     * @param pageable Objeto de paginación
     * @param costumerId Identificador de la cadena
     * @param filter Filtro sobre los atriutos del producto. Opcional
     * @return
     */
    // @GetMapping("/products/byCostumerId/{costumerId}")
    // public ResponseEntity<?> findByOnlyCustomerId(@PageableDefault(size = 200,page = 0) Pageable pageable,@PathVariable String costumerId,@RequestParam(required=false) String filter){
    //     return productService.findByCustomerId(costumerId,filter, pageable);
    // }
    /**
     * Retorna todos los productos de una cadena en un CSV File.
     * @param costumerId Identificador de la cadena
     * @return CSV File
     */
    // @GetMapping("/products/csv/{costumerId}")
    // public ResponseEntity<?> findByCustomerIdCSV(@PathVariable String costumerId){
    //     return productService.findByCustomerIdCSV(costumerId);
    // }
    
}

