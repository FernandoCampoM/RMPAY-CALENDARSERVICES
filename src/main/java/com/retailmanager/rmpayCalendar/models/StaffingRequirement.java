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
public class StaffingRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String startTime; // "HH:mm"

    @Column(nullable = false)
    private String endTime;   // "HH:mm"

    private Integer monRequired;
    private Integer tueRequired;
    private Integer wedRequired;
    private Integer thuRequired;
    private Integer friRequired;
    private Integer satRequired;
    private Integer sunRequired;
}
