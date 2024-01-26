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
public class Permission {

    @Id
    private Long permissionId;
    @Column(nullable = false, unique = true)
    private String name;

    // Getters and setters
}
