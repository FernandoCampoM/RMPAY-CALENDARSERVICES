package com.retailmanager.rmpaydashboard.services.DTO;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TerminalDTO {
    private Long terminalId;
    @NotBlank(message = "{terminal.serial.empty}")
    private String serial;
    @NotBlank(message = "{terminal.name.empty}")
    @Size(max = 255, message = "{terminal.name.max}")
    private String name;
    @NotNull(message = "{terminal.enable.null}")
    private Boolean enable;
    private Long idService;
    private Long businesId;
    private LocalDate lastTransmision;
}
