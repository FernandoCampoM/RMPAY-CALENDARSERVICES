package com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;


@Data
public class RMPayAtTheTable_TerminalDTO {
    private Long terminalId;
    
    @NotBlank(message = "{terminalPayAtTheTable.serialNumber.notBlank}")
   
    private String serialNumber;
    
    @NotNull(message = "{terminalPayAtTheTable.active.notNull}")
    private Boolean active;
    
    @PastOrPresent(message = "{terminalPayAtTheTable.lastTransmissionDate.pastOrPresent}")
    private LocalDate lastTransmissionDate;
    
    @NotNull(message = "{terminalPayAtTheTable.registrationDate.notNull}")
    @PastOrPresent(message = "{terminalPayAtTheTable.registrationDate.pastOrPresent}")
    private LocalDate registrationDate;

    private Long userId; 
    
}
