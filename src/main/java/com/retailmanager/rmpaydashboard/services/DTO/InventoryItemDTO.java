package com.retailmanager.rmpaydashboard.services.DTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventoryItemDTO {
    private Long productId;
    private String description;
    private int quantity;
    private BigDecimal totalCost;
    private BigDecimal unitaryCost;
    private BigDecimal price;
}
