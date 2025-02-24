package com.retailmanager.rmpaydashboard.services.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSchedulesDTO {
    private Long asId;
    private String title;
    private String duration; // Ejemplo: "01:30"
    private Long employeeId; // Relación con UsersBusiness
}
