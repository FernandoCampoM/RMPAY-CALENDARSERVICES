package com.retailmanager.rmpaydashboard.models;

import java.time.LocalDate;
import java.time.LocalTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class EntryExit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userBusinessId", nullable = false)
    private UsersBusiness userBusiness;

    @Column( nullable = false)
    private LocalTime hour;

    @Column( nullable = false)
    private LocalDate date;

    @Column( nullable = false)
    private Boolean entry;
}
