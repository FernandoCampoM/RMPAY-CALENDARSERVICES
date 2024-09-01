package com.retailmanager.rmpaydashboard.services.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventoryItemDTO {
    private Long productId;
    private String description;
    private int quantity;
    private Double totalCost;
    private Double unitaryCost;
    private Double price;
}
