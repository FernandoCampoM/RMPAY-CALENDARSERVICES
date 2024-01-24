package com.retailmanager.rmpaydashboard.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Permission") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Permission {

    @Id
    private Long permissionId;

    private String name;

    // Getters and setters
}
