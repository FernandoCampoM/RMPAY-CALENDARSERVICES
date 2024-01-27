package com.retailmanager.rmpaydashboard.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;import jakarta.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    @Column(nullable = true, unique = false)
    private String barcode;
    @Column(nullable = false, unique = false)
    private String name;
    @Column(nullable = true, unique = false)
    private String description;
    @Column(nullable = false, unique = false)
    private double cost;
    @Column(nullable = false, unique = false)
    private double price;
    
    @Column(nullable = true, unique = false)
    private String code;
    
    // Inventory attributes
    @Column(nullable = false, unique = false)
    private boolean estatal;
    @Column(nullable = false, unique = false) 
    private boolean municipal; 
    @Column(nullable = false, unique = false)
    private int inventoryLevel; 
    @Column(nullable = false, unique = false)
    private int minimumLevel;
    @Column(nullable = false, unique = false) 
    private int maximumLevel;
    @Column(nullable = false)
    private boolean enable=false;
    @ManyToOne(cascade=CascadeType.ALL,optional = true)
    @JoinColumn( name="categoryId",nullable = false)
    private Category category;
}