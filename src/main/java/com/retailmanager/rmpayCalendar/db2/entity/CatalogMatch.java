package com.retailmanager.rmpayCalendar.db2.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.retailmanager.rmpayCalendar.enums.MatchStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Fila de conciliación entre una variante de Shopify y un producto de Retail Manager.
 * matchKey = "S:" + shopifyVariantId para filas de Shopify, "R:" + código RM para productos solo en RM.
 * La relación definitiva vive en ProductSync; esta tabla es el área de trabajo de la pantalla.
 */
@Entity
@Data
@Table(name = "CatalogMatch", indexes = {
        @Index(name = "UX_CatalogMatch_matchKey", columnList = "matchKey", unique = true),
        @Index(name = "IX_CatalogMatch_status", columnList = "status")
})
public class CatalogMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String matchKey;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MatchStatus status;

    /** CODIGO_REFERENCIA, CODIGO_BARCODE, NOMBRE, MANUAL, SYNC */
    @Column(length = 30)
    private String method;

    private Double score;

    // Shopify
    private String shopifyProductId;
    private String shopifyVariantId;
    private String shopifyInventoryItemId;
    @Column(length = 300)
    private String shopifyHandle;
    @Column(length = 500)
    private String shopifyTitle;
    @Column(length = 300)
    private String shopifyVariantTitle;
    @Column(length = 20)
    private String shopifyStatus;
    private String shopifySku;
    private String shopifyBarcode;
    @Column(precision = 19, scale = 2)
    private BigDecimal shopifyPrice;
    private Integer shopifyQty;
    private Integer shopifyVariantCount;

    // Retail Manager
    private String rmProductCode;
    @Column(length = 500)
    private String rmName;
    @Column(precision = 19, scale = 2)
    private BigDecimal rmPrice;
    private Integer rmStock;
    private String rmBarcode;

    @Column(length = 1000)
    private String note;

    private LocalDateTime updatedAt;
    private String updatedBy;
}
