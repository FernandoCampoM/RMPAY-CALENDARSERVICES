package com.retailmanager.rmpaydashboard.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userPermissionid;

    @ManyToOne
    @JoinColumn(name = "userID")
    private UsersBusiness userBusiness;

    @ManyToOne
    @JoinColumn(name = "permissionId")
    private Permission permission;

    @Column(nullable = false)
    private boolean enable;

    // Getters and setters
}