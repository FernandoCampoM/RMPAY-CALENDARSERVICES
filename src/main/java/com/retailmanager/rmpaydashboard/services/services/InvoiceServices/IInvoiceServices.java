package com.retailmanager.rmpaydashboard.services.services.InvoiceServices;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

public interface IInvoiceServices {
    public ResponseEntity<?> getPaymentHistoryByBusiness(Long businessId, LocalDate startDate, LocalDate endDate);
}
