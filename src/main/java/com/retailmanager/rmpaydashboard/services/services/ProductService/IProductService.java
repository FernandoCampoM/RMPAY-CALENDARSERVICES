package com.retailmanager.rmpaydashboard.services.services.ProductService;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.ProductDTO;


public interface IProductService {
    /**
     * Persiste u nuevo producto
     * @param prmProduct Nuevo producto
     * @return Objeto del producto persistido
     */
    public ResponseEntity<?> save(ProductDTO prmProduct);
    /**
     * Persiste una lista de Productos Pertenecientes a una cadena 
     * @param listProducts Lista de nuevos productos
     * @return Lista de productos persistidos
     */
    public ResponseEntity<?> save(String costumerId,List<ProductDTO> listProducts);
    /**
     * Actualiza la información de un producto existente
     * @param prmProduct Objeto del producto
     * @return Objeto del producto actualizado
     */
    public ResponseEntity<?> update(ProductDTO prmProduct);
    /**
     * Elimina el producto especificado.
     * @param productCode Codigo del producto
     * @return True si es exitoso. False de lo contrario.
     */
    public ResponseEntity<?> delete(Long productId);
    /**
     * Consulta un producto especificado
     * @param productCode Codigo del producto
     * @return Objeto del producto consultado
     */
    public ResponseEntity<?> findById(Long productId);
    public ResponseEntity<?> updateEnable(Long productId, boolean enable);
    // /**
    //  * Consulta los productos de una cadena especificada.
    //  * @param costumerId Identificador de la cadena.
    //  * @param pageable Obejto de paginación.
    //  * @return Objeto de la pagina de resultados
    //  */
    // public ResponseEntity<?> findByCustomerId(String costumerId,String filter,Pageable pageable);
    // /**
    //  * Consulta todos los productos con posibilidad de filtrar los asiciados de una cadena. 
    //  * Y un filtro sobre los atributos 
    //  * @param costumerId Identificador de la cadena. Si el costumerId es Null retorna los productos pertenecientes a RetailManager
    //  * @param filter Filtro sobre los atributos del prodcuto
    //  * @param pageable Objeto de paginación.
    //  * @return Pagina de resultados.
    //  */
    // public ResponseEntity<?> findAllAndFilterCustomerId(String costumerId,String filter,Pageable pageable);
    // /**
    //  * Consulta los productos de una cadena especificada y los retorna en un CSV File.
    //  * @param costumerId Identificador de la cadena.
    //  * @return CSV File
    //  */
    // public ResponseEntity<?> findByCustomerIdCSV(String costumerId);
}
