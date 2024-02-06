package com.retailmanager.rmpaydashboard.services.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RejectedPaymentsDTO {
    private int invoiceNumber;
    private double totalAmount;
}
