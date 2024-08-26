package com.retailmanager.rmpaydashboard.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ResellerPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId ;
    private LocalDate date;
    private LocalTime time;
    private String paymentMethod;
    private Double total;
    @ManyToOne(optional = true)
    @JoinColumn( name="resellerId",nullable = false)
    private Reseller reseller;

    @OneToMany(mappedBy = "resellerPayment",fetch = FetchType.LAZY)
    private List<ResellerSales> resellerSales;
}
