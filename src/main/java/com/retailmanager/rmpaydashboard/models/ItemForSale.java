package com.retailmanager.rmpaydashboard.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ItemForSale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;
    private Long productId;
    @Column(nullable = false, unique = false)
    private String barcode;
    @Column(nullable = false, unique = false)
    private String name;
    @Column(nullable = false, unique = false)
    private double cost;
    @Column(nullable = false, unique = false)
    private double price;
    
    @Column(nullable = false, unique = false)
    private String code;
    
    // Inventory attributes
    @Column(nullable = false, unique = false)
    private boolean estatal;
    @Column(nullable = false, unique = false) 
    private boolean municipal;
    @Column(nullable = false, unique = false) 
    private boolean reducedTax;

    private double grossProfit;
    private int quantity;
    private String category;

    @ManyToOne(cascade=CascadeType.PERSIST,optional = true)
    @JoinColumn( name="saleID",nullable = false)
    private Sale sale;
}
