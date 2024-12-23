package com.retailmanager.rmpaydashboard.models;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
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
public class ScheduleCalendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;
    @Column(nullable = false)
    String title;
    @Column(nullable = false)
    LocalDateTime dateStart;
    @Column(nullable = false)
    LocalDateTime dateEnd;
    @Column(nullable = false)
    String color;

    @ManyToOne(cascade=CascadeType.PERSIST,optional = false)
    @JoinColumn( name="userBusinessId",nullable = false)
    private UsersBusiness employee;
}
