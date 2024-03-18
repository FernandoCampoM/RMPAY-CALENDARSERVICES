package com.retailmanager.rmpaydashboard.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // Reemplaza TABLE6_NAME con el nombre real de tu tabla en la base de datos
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Transactions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String paymentType;

    @Column(nullable = false)
    private Double amount;

    @Column
    private String state;

    

    private Double changeChash;

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String refId = "";

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String account = "";

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String cardType = "";

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String authCode = "";

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String batchNo = "";

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String entryMode = "";

    @Column(columnDefinition = "TEXT DEFAULT ''")
    private String globalUid = "";

    @ManyToOne(cascade=CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "saleId", nullable = false)
    private Sale sale;
    // Constructor, getters y setters
}

