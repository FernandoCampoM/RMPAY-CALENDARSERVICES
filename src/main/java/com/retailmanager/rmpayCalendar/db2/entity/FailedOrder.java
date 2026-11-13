package com.retailmanager.rmpayCalendar.db2.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Pedido de Shopify (PAID) cuya factura no llegó a RM. Se reintenta en cada ciclo de
 * InvoiceSyncJob hasta un máximo de intentos; después queda para revisión en el panel.
 * Guarda el pedido tal como lo devolvió la consulta GraphQL para volver a mapearlo.
 */
@Entity
@Data
@Table(name = "FailedOrder")
public class FailedOrder {

    /** ID de Shopify (gid://shopify/Order/...), igual que ProcessedOrder.id */
    @Id
    private String id;

    @Column(length = 50)
    private String orderName;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String orderJson;

    private int attempts;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String lastError;

    private LocalDateTime firstFailedAt;

    private LocalDateTime lastAttemptAt;
}
