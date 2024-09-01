package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventoryReceiptDTO {
    private Long receiptId;
    private String comments;
    
    private String inventoryEntered;
    private LocalDateTime registerDate;
    private Long businessId;

    private List<InventoryItemDTO> inventoryItems;
}
