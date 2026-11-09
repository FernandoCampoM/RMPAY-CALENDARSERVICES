package com.retailmanager.rmpayCalendar.db2.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Traza de cada petición recibida en /webhook/* (Shopify): qué llegó, qué se hizo, qué se
 * respondió y el error si lo hubo. La llena WebhookLogFilter con lo que va anotando WebhookTrace.
 * Se borra sola después de webhook.log.retention-days (ver WebhookLogService).
 */
@Entity
@Data
@Table(name = "WebhookLog", indexes = {
        @Index(name = "IX_WebhookLog_receivedAt", columnList = "receivedAt"),
        @Index(name = "IX_WebhookLog_shopifyOrderId", columnList = "shopifyOrderId") })
public class WebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime receivedAt;

    /** Ruta llamada, por ejemplo /webhook/orders */
    @Column(length = 100)
    private String endpoint;

    /** Header X-Shopify-Topic, por ejemplo orders/paid */
    @Column(length = 100)
    private String topic;

    /** Header X-Shopify-Webhook-Id: Shopify reusa el mismo id en sus reintentos */
    @Column(length = 100)
    private String webhookId;

    /** Header X-Shopify-Shop-Domain */
    @Column(length = 150)
    private String shopDomain;

    private Boolean hmacPresent;

    private Boolean hmacValid;

    @Column(length = 100)
    private String shopifyOrderId;

    @Column(length = 50)
    private String orderName;

    /** PROCESADA, DUPLICADA, IGNORADA, RECIBIDA, FIRMA_INVALIDA, SIN_FIRMA, PETICION_INVALIDA, ERROR, OK */
    @Column(length = 40)
    private String result;

    /** Qué se hizo o se guardó, en texto */
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String detail;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String payload;

    /** Factura enviada a RM (JSON) */
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String invoiceJson;

    private Integer httpStatus;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String responseBody;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String error;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String stackTrace;

    private Long durationMs;
}
