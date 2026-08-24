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
public class EmployeeAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeID;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String notes;

    private Integer maxWeeklyHours; // null = no cap

    // Monday
    private Boolean monEnabled;
    private String monStart;
    private String monEnd;

    // Tuesday
    private Boolean tueEnabled;
    private String tueStart;
    private String tueEnd;

    // Wednesday
    private Boolean wedEnabled;
    private String wedStart;
    private String wedEnd;

    // Thursday
    private Boolean thuEnabled;
    private String thuStart;
    private String thuEnd;

    // Friday
    private Boolean friEnabled;
    private String friStart;
    private String friEnd;

    // Saturday
    private Boolean satEnabled;
    private String satStart;
    private String satEnd;

    // Sunday
    private Boolean sunEnabled;
    private String sunStart;
    private String sunEnd;
}
