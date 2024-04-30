package com.retailmanager.rmpaydashboard.models;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UsersBusiness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userBusinessId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean enable;

    @OneToMany(mappedBy = "userBusiness", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPermission> userPermissions;

    @ManyToOne(cascade=CascadeType.PERSIST,optional = false)
    @JoinColumn( name="businessId",nullable = false)
    private Business business;

    private Boolean download=false;

    

}