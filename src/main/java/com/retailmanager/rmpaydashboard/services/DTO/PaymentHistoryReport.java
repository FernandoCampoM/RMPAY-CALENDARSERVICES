package com.retailmanager.rmpaydashboard.services.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.cglib.core.Local;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor @NoArgsConstructor @Getter @Setter
public class PaymentHistoryReport {
    private String name;
    private String businessName;
    private String merchantId;
    private Long invoiceId;
    private LocalDate date; // Asumiendo que "fecha" es de tipo Date
    private LocalTime time; // Asumiendo que "hora" es de tipo Time
    private String payMethod;
    private int numberTerminals;
    private Double total; // Asumiendo que "total" es un valor monetario
    private String reference;
}
