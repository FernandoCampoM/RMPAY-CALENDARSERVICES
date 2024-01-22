package com.retailmanager.rmpaydashboard.models;

import com.retailmanager.rmpaydashboard.enums.Rol;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userID;
    @Column(nullable = false)
    private String name;
    @Column(nullable = true)
    private String phone;
    @Column(nullable = true)
    private String businessName;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private boolean enabled=false;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,columnDefinition = "VARCHAR(14)")
    private Rol roles;
    /**Objeto que encapsula la información del usuario asociado a la cadena */
    @OneToOne(cascade=CascadeType.ALL,optional = true)
    @JoinColumn( name="addressId",nullable = true)
    private Address address;
}
