package com.retailmanager.rmpaydashboard.services.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BusinessConfigurationMini {
    private Long Configurationid;

    private String configKey;  // Name of the configuration (e.g., 'MaxUsers')

    private String value;
    private String configName;
}
