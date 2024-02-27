package com.retailmanager.rmpaydashboard.models;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long terminalId;
    @Column(nullable = true)
    private String serial;
    @Column(nullable = true)
    private String name;

    @Column(nullable = false)
    private boolean enable;

    @ManyToOne(cascade=CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "businessId")
    private Business business;
    @ManyToOne(cascade=CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "serviceId")
    private Service service;
    @Column(nullable = true)
    private LocalDate expirationDate;

    private boolean automaticPayments;
    private boolean isPayment;
    private boolean isPrincipal;
    
    // Otros campos y métodos según se necesite
}
