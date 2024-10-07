package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BusinessConfigurationDTO {
    private Long Configurationid;

    private Long businessId;

    private String configKey;  // Name of the configuration (e.g., 'MaxUsers')

    private String value;  // Text or long string configuration value with 'TEXT' type
    private String configName;

    private LocalDateTime createdAt ;  // Creation timestamp

    private LocalDateTime updatedAt;
}
