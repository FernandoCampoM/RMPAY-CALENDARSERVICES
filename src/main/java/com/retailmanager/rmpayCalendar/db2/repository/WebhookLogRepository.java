package com.retailmanager.rmpayCalendar.db2.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpayCalendar.db2.entity.WebhookLog;

/**
 * Traza de los webhooks de Shopify (ver WebhookLogFilter).
 */
public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {

    /** Busca en pedido, número, endpoint, tema, resultado y error. */
    @Query("SELECT w FROM WebhookLog w WHERE (:q IS NULL"
            + " OR LOWER(w.shopifyOrderId) LIKE :q OR LOWER(w.orderName) LIKE :q OR LOWER(w.endpoint) LIKE :q"
            + " OR LOWER(w.topic) LIKE :q OR LOWER(w.result) LIKE :q OR LOWER(w.error) LIKE :q)")
    Page<WebhookLog> search(@Param("q") String q, Pageable pageable);

    /** Último registro recibido en una ruta (para mostrar si el webhook ya está llegando). */
    WebhookLog findFirstByEndpointOrderByReceivedAtDesc(String endpoint);

    @Modifying
    @Transactional
    @Query("DELETE FROM WebhookLog w WHERE w.receivedAt < :before")
    int deleteOlderThan(@Param("before") LocalDateTime before);
}
