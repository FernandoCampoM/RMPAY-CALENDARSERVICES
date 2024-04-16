package com.retailmanager.rmpaydashboard.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserBusiness_Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long UBP_ID;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId",nullable = false)
    private Product objProduct;
    @ManyToOne
    @JoinColumn(name = "userBusinessId",nullable = false)
    private UsersBusiness objUser;
    private Boolean download=false;
}
