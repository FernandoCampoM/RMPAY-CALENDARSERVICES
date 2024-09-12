package com.retailmanager.rmpaydashboard.services.services.InvoiceServices;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.ConfirmPaymentDTO;
import com.retailmanager.rmpaydashboard.services.DTO.PaymentDataDTO;
import com.retailmanager.rmpaydashboard.services.DTO.doPaymentDTO;

public interface IInvoiceServices {
    public ResponseEntity<?> createToken(PaymentDataDTO prmPaymentInfo);
    public ResponseEntity<?> existToken(Long prmBusinessId);
    public ResponseEntity<?> getToken(Long prmBusinessId);
    public ResponseEntity<?> deleteToken(Long prmBusinessId);
    public ResponseEntity<?> doPayment(doPaymentDTO prmPaymentInfo);
    public ResponseEntity<?> getPaymentHistoryByBusiness(Long businessId, LocalDate startDate, LocalDate endDate);
    public ResponseEntity<?> testPayment(doPaymentDTO prmPaymentInfo);
    public ResponseEntity<?> getPaymentHistor(LocalDate startDate, LocalDate endDate, String filter);
    public ResponseEntity<?> confirmOrRejectPaymnt(Long invoiceNumber,ConfirmPaymentDTO prmPaymentInfo);
}
