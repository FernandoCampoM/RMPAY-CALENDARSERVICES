package com.retailmanager.rmpaydashboard.services.DTO;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResellerAcountReport {
    private String resellerName;
    private Long resellerSalesId;
    private Long resellerId;
    private String merchantId;
    private String clientName;
    private Double commission;
    private String detailService;
    private Double totalService;
    private LocalDate paymentDate;
}
