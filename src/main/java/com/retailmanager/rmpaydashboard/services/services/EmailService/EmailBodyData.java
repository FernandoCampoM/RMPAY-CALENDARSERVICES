package com.retailmanager.rmpaydashboard.services.services.EmailService;

import java.util.ArrayList;
import java.util.List;

import com.retailmanager.rmpaydashboard.services.DTO.RejectedPaymentsDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalsDoPaymentDTO;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmailBodyData {
    //atributo para saber si se aplicó algún descuento
    private double discount=0.0;
    private String name;
    private String merchantId;
    private String businessName;
    private Integer additionalTerminals;
    private double additionalTerminalsValue;
    private String paymethod;
    private String phone;

    private boolean buyTerminal;
    private String subject="";
    //PARA PAGO CON TARJETA
    private String nameoncard;
    private String creditcarnumber;
    private String securitycode;    
            
    private String expDateMonth;
    private String expDateYear;

    private double amount;
    private double subTotal;
    private boolean automaticPayments;
    
    private List<RejectedPaymentsDTO> rejectedPayments= new ArrayList<>();

    private String serviceDescription;
    private String serviceValue;
    private String referenceNumber="";
    private String cardType;
    private String email;
    private Long invoiceNumber;

    private String accountNameBank;
    private String accountNumberBank;
    private String routeNumberBank;
    private Long chequeVoidId;

    private List<TerminalsDoPaymentDTO> terminalsDoPayment;
}
