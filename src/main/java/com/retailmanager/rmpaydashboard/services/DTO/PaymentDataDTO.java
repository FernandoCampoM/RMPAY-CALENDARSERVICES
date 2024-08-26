package com.retailmanager.rmpaydashboard.services.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaymentDataDTO {
private int paymentId;
    
    private String token;
    private String expDate;
    @NotEmpty(message = "{paymentdata.nameoncard.notempty}")
    private String nameOnCard;
    private String cvn;
    private String last4Digits;


    @NotEmpty(message = "{paymentdata.creditcardnumber.notempty}")
    private String creditcarnumber;

    @NotEmpty(message = "{paymentdata.securitycode.notempty}")
    private String securitycode;  
    @NotEmpty(message = "{paymentdata.cardType.notempty}")
    private String cardType;
    @NotEmpty(message = "{paymentdata.expDateMonth.notempty}")        
    private String expDateMonth;
    @NotEmpty(message = "{paymentdata.expDateYear.notempty}")
    private String expDateYear;
    @NotEmpty(message = "{paymentdata.zipCode.notempty}")
    private String zipCode;
    @Positive(message = "{paymentdata.businessId.notempty}")
    private Long businessId;
}
