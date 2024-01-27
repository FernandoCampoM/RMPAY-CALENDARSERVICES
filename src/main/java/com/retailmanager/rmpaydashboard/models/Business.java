package com.retailmanager.rmpaydashboard.models;

import java.util.List;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Getter @Setter @NoArgsConstructor  @AllArgsConstructor
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long businessId;
    @Column(nullable = true)
    private String merchantId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String businessPhoneNumber;
    @Column(nullable = false)
    private Integer additionalTerminals;
    @Column(nullable = false)
    private boolean enable=false;

    /**Objeto que encapsula la información de la dirección */
    @OneToOne(cascade=CascadeType.ALL,optional = true)
    @JoinColumn( name="addressId",nullable = true)
    private Address address;

    @ManyToOne(optional = false)
    @JoinColumn( name="userId",nullable = false)
    private User user;

    @OneToMany(mappedBy = "business",fetch = FetchType.LAZY)
    private List<Category> categories;
    
    @OneToMany(mappedBy = "business",fetch =FetchType.LAZY)
    private List<Terminal> terminals;
    

}