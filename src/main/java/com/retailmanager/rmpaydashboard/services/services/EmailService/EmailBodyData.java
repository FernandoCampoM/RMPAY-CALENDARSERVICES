package com.retailmanager.rmpaydashboard.services.services.EmailService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.retailmanager.rmpaydashboard.services.DTO.RejectedPaymentsDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmailBodyData {
    private String name;
    private String merchantId;
    private String businessName;
    private Integer additionalTerminals;
    private double additionalTerminalsValue;
    private String paymethod;
    //PARA PAGO CON TARJETA
    private String nameoncard;
    private String creditcarnumber;
    private String securitycode;    
            
    private String expDateMonth;
    private String expDateYear;

    private double amount;
    private boolean automaticPayments;
    
    private List<RejectedPaymentsDTO> rejectedPayments= new ArrayList<>();

    private String serviceDescription;
    private String serviceValue;
    private LocalDate expirationDate;
    private String referenceNumber;
    private String cardType;
    private String email;
    private Long invoiceNumber;

    private String accountNameBank;
    private String accountNumberBank;
    private String routeNumberBank;
}
