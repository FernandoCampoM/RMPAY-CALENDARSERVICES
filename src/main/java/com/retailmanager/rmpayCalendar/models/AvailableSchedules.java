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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSchedules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long asId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String duration; // Ejemplo: "01:30" (HH:mm)

    private Long employeeID;
}
