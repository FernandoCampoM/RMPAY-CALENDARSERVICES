package com.retailmanager.rmpaydashboard.services.DTO;

import java.util.List;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventoryReceiptDTO {
    private Long receiptId;
    private String comments;
    
    private String inventoryEntered;

    private List<InventoryItemDTO> inventoryItems;
}
