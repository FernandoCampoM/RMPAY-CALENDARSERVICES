package com.retailmanager.rmpaydashboard.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter // Lombok annotation to generate getters, setters, equals, hash, toString
@NoArgsConstructor  // Lombok annotation to generate no-args constructor
public class BusinessConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Configurationid;

    
    @ManyToOne(optional = false)
    @JoinColumn( name="businessId",nullable = false)
    private Business business;

    @Column(nullable = false, length = 100)
    private String configKey;  // Name of the configuration (e.g., 'MaxUsers')

    @Column(columnDefinition = "TEXT")
    private String value;  // Text or long string configuration value with 'TEXT' type

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();  // Creation timestamp

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();  // Update timestamp

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
