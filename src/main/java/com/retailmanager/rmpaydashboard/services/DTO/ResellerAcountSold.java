package com.retailmanager.rmpaydashboard.services.DTO;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ResellerAcountSold {
    private Long resellerSalesId;
    private String merchantId;
    private String merchantName;
    private LocalDate expirationDate;
    private LocalDate lastTransmission;
}
