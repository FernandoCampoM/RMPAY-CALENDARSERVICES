package com.retailmanager.rmpayCalendar.db2.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.retailmanager.rmpayCalendar.db2.entity.ProcessedOrder;

import jakarta.transaction.Transactional;
public interface ProcessedOrderRepository extends CrudRepository<ProcessedOrder, String>{

    /**
     * Reserva el pedido con un INSERT (no un merge como save): si otro camino ya lo guardó, la
     * clave primaria lo rechaza con DataIntegrityViolationException. Lo usa el webhook de pago
     * antes de enviar la factura a RM, para que el mismo pedido no se facture dos veces.
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ProcessedOrder (id, processedAt, JSONRecibed, eventType, totalAmount) "
            + "VALUES (:id, :processedAt, :json, :eventType, :total)", nativeQuery = true)
    int reserve(@Param("id") String id, @Param("processedAt") LocalDateTime processedAt, @Param("json") String json,
            @Param("eventType") String eventType, @Param("total") BigDecimal total);

    /** Guarda el número de factura con que quedó el pedido en RM (K… o KW…). */
    @Modifying
    @Transactional
    @Query(value = "UPDATE ProcessedOrder SET rmInvoice = :invoice WHERE id = :id", nativeQuery = true)
    int setRmInvoice(@Param("id") String id, @Param("invoice") String invoice);
}
