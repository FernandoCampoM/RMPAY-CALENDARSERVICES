package com.retailmanager.rmpaydashboard.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.retailmanager.rmpaydashboard.models.Product;



public interface ProductRepository extends CrudRepository<Product,Long>,PagingAndSortingRepository<Product,Long> {
    /**
     * Obtiene los productos que no están asociados a una cadena
     * @param pageable Objeto para paginación
     * @return Objeto de pagina de resultados
     
    public Page<Product> findByobjCustomerIsNull(Pageable pageable);*/
    /**
     * Obtiene los productos asociados a la cadena especificada o que no están asociados a una cadena
     * @param objCostumer Cadena de la cual se quieren recupear los productos
     * @param pageable Objeto de paginación
     * @return Objeto de pagina de resultados
     
    public Page<Product> findByobjCustomerIsOrObjCustomerIsNull(Customer objCostumer,Pageable pageable);
    */
    /**
     * Obtiene los productos de una cadena especificada
     * @param objCostumer Cadena de la cual se quieren recupear los productos
     * @param pageable  Objeto de paginación
     * @return Objeto de pagina de resultados
     
    public Page<Product> findByobjCustomerIs(Customer objCostumer,Pageable pageable);*/
    /**
     * Recupera la información de un producto por codigo de producto o codigo de barras
     * @param productCode Codigo del producto
     * @param barCode Codigo de barras
     * @return Opcional del producto
     */
    public Optional<Product> findOneByCodeOrBarcode(String productCode, String barCode);
    /**
     * Obtiene los productos por categoria
     * @param category Categoria de la cual se quieren recupear los productos
     * @param pageable Objeto de paginación
     * @return
     */
    public Page<Product> findByCategory(String category, Pageable pageable);
    /**
     * Obtiene los productos por nombre
     * @param name Nombre del cual se quieren recupear los productos
     * @param pageable Objeto de paginación
     * @return
     */
    public Page<Product> findByName(String name, Pageable pageable);
    @Modifying
    @Query("UPDATE Product u SET u.enable = :enable WHERE u.productId = :productId")
    void updateEnable(Long productId, boolean enable);
    /**
     * Obtiene los productos aplicando un filtro sobre sus atributos: productCode,  barCode, productName, description, provider, department, category
     * @param costumerId Identificador de la cadena.
     * @param filter Filtro para bsucar productos
     * @param pageable Objeto de paginación
     * @return Pagina de resultados
     */
    //@Query(value = "select p from Product p where (p.objCustomer is null or p.objCustomer.customerId=:costumerId) and (p.productCode like :filter or p.barCode like :filter or p.productName like :filter or p.description like :filter or p.provider like :filter or p.department like :filter or p.category like :filter) ORDER BY p.objCustomer desc")
    //public Page<Product> findProductsByFilterAndCustomerId(String costumerId, String filter,Pageable pageable);
    /**
     * Obtiene los productos de una cadena aplicando un filtro sobre sus atributos: productCode,  barCode, productName, description, provider, department, category
     * y sólo se aplica el filtro sobre los productos de la cadena specificada
     * @param costumerId Identificador de la cadena.
     * @param filter Filtro para bsucar productos
     * @param pageable Objeto de paginación
     * @return Pagina de resultados
     */
    //@Query(value = "select p from Product p where p.objCustomer.customerId=:costumerId and (p.productCode like :filter or p.barCode like :filter or p.productName like :filter or p.description like :filter or p.provider like :filter or p.department like :filter or p.category like :filter)")
    //public Page<Product> findProductsByFilterAndOnlyCustomerId(String costumerId, String filter,Pageable pageable);
    /**
     * Obtiene los productos que no pertenecen a una cadena aplicando un filtro sobre sus atributos: productCode,  barCode, productName, description, provider, department, category
     * @param filter Filtro para bsucar productos
     * @param pageable Objeto de paginación
     * @return  Pagina de resultados
     */
    //@Query(value = "select p from Product p where p.objCustomer is null and (p.productCode like :filter or p.barCode like :filter or p.productName like :filter or p.description like :filter or p.provider like :filter or p.department like :filter or p.category like :filter)")
    //public Page<Product> findProductsByFilter(String filter,Pageable pageable);
    // /**
    //  * Obtiene todos los productos de una cadena 
    //  * @param costumerId Identificador de la cadena.
    //  * @return Pagina de resultados
    //  */
    // @Query(value = "select p from Product p where p.objCustomer.customerId=:costumerId")
    // public List<Product> findAllProductsByCustomerId(String costumerId);
    // /**
    //  * Obtiene todos los productos de retail manager, es decir no están asociados a una cadena.
    //  * @return Pagina de resultados
    //  */
    // @Query(value = "select p from Product p where p.objCustomer is null")
    // public List<Product> findAllProductsRetailManager();
    //@Query(value = "select count(p) from Product p where p.objCustomer.customerId=:costumerId")
    //public int countByCostumerId(String costumerId);
}
