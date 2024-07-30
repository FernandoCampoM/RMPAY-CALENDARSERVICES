package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EntryExitDTO {
    private Long id;
    //Identificador del userBusiness es decir del empleado
    //Diferente a los usuaros de la app web
    @NotNull(message = "{entryexit.userBusinessId.notNull}")
    private Long userId;

    private String name;

    @NotNull(message = "{entryexit.hour.notNull}")
    private LocalTime hour;

    @NotNull(message = "{entryexit.date.notNull}")
    private LocalDate date;

    @NotNull(message = "{entryexit.entry.notNull}")
    private Boolean entry;
    private Double totalWorkCost;

    
    private float hoursWorked;
    
}
