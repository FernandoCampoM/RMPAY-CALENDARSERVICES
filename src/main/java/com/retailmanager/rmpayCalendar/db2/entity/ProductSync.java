package com.retailmanager.rmpayCalendar.db2.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ProductSync {

    @Id
    @GeneratedValue
    private Long id;

    private String productCode;

    private String shopifyProductId;
    private String shopifyVariantId;
    private String shopifyInventoryItemId;

    private String hash;

    private LocalDateTime lastSync;
     private LocalDateTime lastSeen;
    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
     private Boolean deleted = false;

private LocalDateTime deletedAt;
}