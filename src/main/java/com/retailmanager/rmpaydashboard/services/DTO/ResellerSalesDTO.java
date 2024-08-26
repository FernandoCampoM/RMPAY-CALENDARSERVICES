package com.retailmanager.rmpaydashboard.services.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ResellerSalesDTO {
    private Long resellerSalesId;
    private Long resellerId;
    private String merchantId;
    private String paymentDate;
    private Double commission;
    private String detailService;
    private Double totalService;
    private Long invoiceId;
}
