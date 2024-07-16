package com.retailmanager.rmpaydashboard.services.services.Payment.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class PaymentDetailsDTO {
    private String account;
    private String expDate;
    private String nameOnCard;
    private String street;
    private String zipCode;
    private String cv;
    private String clientRef;
    private String description;
    private String existingToken;
    private String appKey;
    private Integer appType;
    private Integer mid;
    private Integer cid;
    private String userName;
    private String password;
    private String hostUserName;
    private String hostPassword;
    private Integer logId;
    private Integer userId;
    private String ipAddress;
}
