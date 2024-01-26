package com.retailmanager.rmpaydashboard.models;
import lombok.*;

import jakarta.persistence.*;
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @Column(nullable = false)
    private String serviceName;

    @Column
    private String serviceDescription;

    @Column(nullable = false)
    private Double serviceValue;

    @Column(nullable = false)
    private boolean enable;

    @Column
    private Double referralPayment;

    @Column(nullable = false)
    private Integer duration;
    @Column(nullable = false)
    private double terminals2to5;

    @Column(nullable = false)
    private double referralPayment2to5;

    @Column(nullable = false)
    private double terminals6to9;

    @Column(nullable = false)
    private double referralPayment6to9;

    @Column(nullable = false)
    private double terminals10;

    @Column(nullable = false)
    private double referralPayment10;
}