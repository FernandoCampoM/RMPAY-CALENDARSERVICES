package com.retailmanager.rmpayCalendar.db2.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpayCalendar.db2.entity.FailedOrder;

/**
 * Facturas de pedidos de Shopify que RM rechazó o no recibió (ver InvoiceSyncService).
 */
public interface FailedOrderRepository extends JpaRepository<FailedOrder, String> {

    /** Pendientes de reintento: los que todavía no llegan al máximo de intentos. */
    List<FailedOrder> findByAttemptsLessThanOrderByFirstFailedAtAsc(int maxAttempts);

    @Query("SELECT f FROM FailedOrder f WHERE (:q IS NULL OR LOWER(f.id) LIKE :q OR LOWER(f.orderName) LIKE :q OR LOWER(f.lastError) LIKE :q)")
    Page<FailedOrder> search(@Param("q") String q, Pageable pageable);
}
