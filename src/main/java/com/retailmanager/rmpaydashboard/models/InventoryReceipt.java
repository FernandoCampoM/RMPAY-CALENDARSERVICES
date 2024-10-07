package com.retailmanager.rmpaydashboard.models;

import java.time.LocalDateTime;

import com.retailmanager.rmpaydashboard.services.DTO.InventoryReceiptDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventoryReceipt {
    @Id
    private Long receiptId;
    private String comments;
    @Column(nullable = false,columnDefinition = "VARCHAR(MAX)")
    private String inventoryEntered;
    @Column(nullable = true,columnDefinition = "VARCHAR(MAX)")
    private String supplier;
    private LocalDateTime registerDate;

    @ManyToOne(optional = false)
    @JoinColumn( name="businessId",nullable = false)
    private Business objBusiness;

    public InventoryReceiptDTO toInventoryReceiptDTO() {
        InventoryReceiptDTO inventoryReceiptDTO = new InventoryReceiptDTO();
        inventoryReceiptDTO.setComments(this.comments);
        inventoryReceiptDTO.setInventoryEntered(this.inventoryEntered);
        inventoryReceiptDTO.setRegisterDate(this.registerDate.withNano(0));
        inventoryReceiptDTO.setReceiptId(this.receiptId);
        inventoryReceiptDTO.setBusinessId(this.objBusiness.getBusinessId());
        inventoryReceiptDTO.setSupplier(this.supplier);
         return inventoryReceiptDTO;
    }

    
   
}
