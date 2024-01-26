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
import jakarta.persistence.Column;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long terminalId;
    @Column(nullable = false)
    private String serial;
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean enable;

    @ManyToOne( optional = false)
    @JoinColumn(name = "businessId")
    private Business business;
    // Otros campos y métodos según se necesite
}
