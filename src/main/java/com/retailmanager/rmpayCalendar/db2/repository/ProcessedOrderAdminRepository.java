package com.retailmanager.rmpayCalendar.db2.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpayCalendar.db2.entity.ProcessedOrder;

/**
 * Consultas de solo lectura sobre ProcessedOrder para el panel admin.
 * Convive con ProcessedOrderRepository, que no se modifica.
 */
public interface ProcessedOrderAdminRepository extends JpaRepository<ProcessedOrder, String> {

    @Query("SELECT o FROM ProcessedOrder o WHERE (:q IS NULL OR LOWER(o.id) LIKE :q OR LOWER(o.eventType) LIKE :q OR LOWER(o.rmInvoice) LIKE :q)")
    Page<ProcessedOrder> search(@Param("q") String q, Pageable pageable);
}
