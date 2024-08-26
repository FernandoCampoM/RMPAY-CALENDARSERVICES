package com.retailmanager.rmpaydashboard.models;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
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
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ResellerSales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resellerSalesId;

    @ManyToOne(optional = true)
    @JoinColumn( name="resellerId",nullable = false)
    private Reseller reseller;

    private String merchantId;
    private LocalDate paymentDate;
    private Double commission;
    private String detailService;
    private Double totalService;
    @ManyToOne(optional = true)
    @JoinColumn( name="invoiceNumber",nullable = false)
    private Invoice invoice;
    @ManyToOne( optional = true,cascade= CascadeType.REMOVE)
    @JoinColumn(name = "paymentId", nullable = true)
    private ResellerPayment resellerPayment;
}
