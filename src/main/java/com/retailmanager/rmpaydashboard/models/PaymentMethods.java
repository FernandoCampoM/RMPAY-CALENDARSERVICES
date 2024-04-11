package com.retailmanager.rmpaydashboard.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaymentMethods {

    @Id
    private String code;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Boolean enable;
    @Column(nullable = true)
    private String notes;
}