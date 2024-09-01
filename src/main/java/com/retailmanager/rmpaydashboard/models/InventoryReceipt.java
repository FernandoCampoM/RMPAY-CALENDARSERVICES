package com.retailmanager.rmpaydashboard.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

import com.retailmanager.rmpaydashboard.services.DTO.InventoryReceiptDTO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
         return inventoryReceiptDTO;
    }

    
   
}
