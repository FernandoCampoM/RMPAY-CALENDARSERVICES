package com.retailmanager.rmpayCalendar.db2.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Registro de auditoría del panel admin: quién cambió qué y cuándo.
 */
@Entity
@Data
@Table(name = "AdminAuditLog")
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime at;

    @Column(length = 100)
    private String username;

    @Column(length = 60)
    private String action;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String detail;
}
