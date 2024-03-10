package com.retailmanager.rmpaydashboard.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saleID")
    private Long saleID;

    @Column(name = "saleCreationDate", nullable = false)
    private String saleCreationDate;

    @Column(name = "saleEndDate")
    private LocalDate saleEndDate;

    @Column(name = "saleItems")
    private String items;

    @Column(name = "saleSubtotal", nullable = false)
    private double saleSubtotal;

    @Column(name = "saleStateTaxAmount", nullable = false)
    private double saleStateTaxAmount;

    @Column(name = "saleCityTaxAmount", nullable = false)
    private double saleCityTaxAmount;

    @Column(name = "saleReduceTax", nullable = false)
    private double saleReduceTax;

    @Column(name = "saleTotalAmount", nullable = false)
    private double saleTotalAmount;

    @Column(name = "saleTransactionType", nullable = false)
    private String saleTransactionType;

    @Column(name = "saleMachineID")
    private Integer saleMachineID;

    @Column(name = "saleIvuNumber")
    private String saleIvuNumber;

    @Column(name = "saleStatus")
    private String saleStatus;

    @Column(name = "saleChange")
    private Double saleChange;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @Column(name = "merchantId", nullable = false)
    private String merchantId;

    @Column(name = "saleToRefund")
    private Integer saleToRefund;

    @ManyToOne(cascade=CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "businessId")
    private Business business;

    @ManyToOne(cascade=CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "terminalId")
    private Terminal terminal;

}
