package com.retailmanager.rmpayCalendar.services.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAvailabilityDTO {
    private Long id;
    @NotNull
    private Long employeeID;
    private String notes;
    private Integer maxWeeklyHours;
    private Boolean monEnabled;
    private String monStart;
    private String monEnd;
    private Boolean tueEnabled;
    private String tueStart;
    private String tueEnd;
    private Boolean wedEnabled;
    private String wedStart;
    private String wedEnd;
    private Boolean thuEnabled;
    private String thuStart;
    private String thuEnd;
    private Boolean friEnabled;
    private String friStart;
    private String friEnd;
    private Boolean satEnabled;
    private String satStart;
    private String satEnd;
    private Boolean sunEnabled;
    private String sunStart;
    private String sunEnd;
}
