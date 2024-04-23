package com.retailmanager.rmpaydashboard.services.services.InvoiceServices;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.doPaymentDTO;

public interface IInvoiceServices {
    public ResponseEntity<?> doPayment(doPaymentDTO prmPaymentInfo);
    public ResponseEntity<?> getPaymentHistoryByBusiness(Long businessId, LocalDate startDate, LocalDate endDate);
}
