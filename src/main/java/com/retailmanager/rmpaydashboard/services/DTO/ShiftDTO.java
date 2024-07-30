package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ShiftDTO {
    private Long shiftId;

    private Double initialBalance;
    private Double finalBalance;
    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;

    private String summary;


    //es la password que usa el empleado para autenicrse en la app movil
    @NotNull(message = "{shift.password.notNull}")
    private String password;
}
