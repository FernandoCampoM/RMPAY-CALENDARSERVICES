package com.retailmanager.rmpaydashboard.services.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResellerUnpaidAccounts {
    private Long resellerSalesId;
    private Long idUser;
    private String clientName;
    private Double commission;
    private Double totalService;
}
