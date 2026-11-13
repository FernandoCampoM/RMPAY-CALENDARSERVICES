package com.retailmanager.rmpayCalendar.db2.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;

/**
 * Consultas de solo lectura sobre ProductSync para el panel admin y la conciliación.
 * Convive con ProductSyncRepository, que no se modifica.
 */
public interface ProductSyncAdminRepository extends JpaRepository<ProductSync, Long> {

    long countByDeletedFalse();

    List<ProductSync> findAllByDeletedFalse();

    ProductSync findFirstByShopifyVariantIdAndDeletedFalse(String shopifyVariantId);

    ProductSync findFirstByProductCodeAndDeletedFalse(String productCode);

    @Query("SELECT p FROM ProductSync p WHERE (:q IS NULL OR LOWER(p.productCode) LIKE :q OR LOWER(p.shopifyProductId) LIKE :q) "
            + "AND (:includeDeleted = true OR p.deleted = false)")
    Page<ProductSync> search(@Param("q") String q, @Param("includeDeleted") boolean includeDeleted, Pageable pageable);
}
