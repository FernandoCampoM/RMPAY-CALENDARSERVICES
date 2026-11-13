package com.retailmanager.rmpayCalendar.db2.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedOrder {

    @Id
    private String id;

    private LocalDateTime processedAt;
     
     
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String JSONRecibed;
    private String eventType;
    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    /**
     * NUEVO: número de la factura en RM (K… si la envió el ciclo, KW… si la envió el webhook).
     * Vacío mientras no se envía o si el pedido no se facturó. Lo muestra el panel en Pedidos.
     */
    private String rmInvoice;

    /** Constructor de siempre (sin factura de RM): lo usan el ciclo, los reintentos y el webhook. */
    public ProcessedOrder(String id, LocalDateTime processedAt, String JSONRecibed, String eventType, BigDecimal totalAmount) {
        this(id, processedAt, JSONRecibed, eventType, totalAmount, null);
    }
}