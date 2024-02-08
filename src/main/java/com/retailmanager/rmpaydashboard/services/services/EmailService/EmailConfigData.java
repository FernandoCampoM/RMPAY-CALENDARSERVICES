package com.retailmanager.rmpaydashboard.services.services.EmailService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class EmailConfigData {
    private String appKey;
    private String emailFrom;
    private String emailTo;
    private String emailCCO;
}
