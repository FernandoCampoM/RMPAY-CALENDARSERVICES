package com.retailmanager.rmpaydashboard.models;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.retailmanager.rmpaydashboard.services.DTO.SaleDTO;

import jakarta.persistence.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Reseller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resellerId;
    @Column(nullable = false)
    private String firstname;
    @Column(nullable = false)
    private String lastname;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String company;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String address;
    private boolean status;
    private Long imageprofile;
    private Double commissionsBalance=0.0;
    private String email1;
    private String email2;

    @OneToMany(mappedBy = "reseller",fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    public List<ResellerSales> sales = new ArrayList<>();
}
