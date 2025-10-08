package com.retailmanager.rmpayCalendar.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Sys_general_config {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idconfig;

    @Column(nullable = false)
    private String configname;

    @Column(nullable = false, columnDefinition = "VARCHAR(MAX)")
    private String configvalue;

    @Column(nullable = false)
    private String configlabel;
}
