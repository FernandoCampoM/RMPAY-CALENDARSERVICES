package com.retailmanager.rmpaydashboard.controller;

import java.time.LocalDate;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.services.DTO.ConfirmPaymentDTO;
import com.retailmanager.rmpaydashboard.services.DTO.PaymentDataDTO;
import com.retailmanager.rmpaydashboard.services.DTO.doPaymentDTO;
import com.retailmanager.rmpaydashboard.services.services.InvoiceServices.IInvoiceServices;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api")
@Validated
public class InvoiceController {
    @Autowired
    private IInvoiceServices invoiceService;

    @GetMapping("/invoices/historyByBusiness/{businessId}")
    public ResponseEntity<?> getMethodName(@PathVariable Long businessId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return this.invoiceService.getPaymentHistoryByBusiness(businessId, startDate, endDate);
    }
    
    @GetMapping("/invoices/history")
    public ResponseEntity<?> getMethodName(@RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate,@RequestParam(required = false) String filter) {
        return this.invoiceService.getPaymentHistor(startDate, endDate, filter);
    }
    @PostMapping("/invoices/token")
    public ResponseEntity<?> createToken(@Valid @RequestBody PaymentDataDTO prmPaymentInfo){
        return this.invoiceService.createToken(prmPaymentInfo);
    }
    @GetMapping("/invoices/token/exist")
    public ResponseEntity<?> createToken(@Valid @RequestParam("businessid") String prmBusinessId){
       try {
            Long businessId = Long.parseLong(prmBusinessId);
            return this.invoiceService.existToken(businessId);
        } catch (Exception e) {
            HashMap<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    @GetMapping("/invoices/token")
    public ResponseEntity<?> getToken(@Valid @RequestParam("businessid") String prmBusinessId){
       try {
            Long businessId = Long.parseLong(prmBusinessId);
            return this.invoiceService.getToken(businessId);
        } catch (Exception e) {
            HashMap<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    @DeleteMapping("/invoices/token")
    public ResponseEntity<?> deleteToken(@Valid @RequestParam("businessid") String prmBusinessId){
       try {
            Long businessId = Long.parseLong(prmBusinessId);
            return this.invoiceService.deleteToken(businessId);
        } catch (Exception e) {
            HashMap<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    @PostMapping("/invoices/doPayment")
    public ResponseEntity<?> doPayment(@Valid @RequestBody doPaymentDTO prmPaymentInfo){
        return this.invoiceService.doPayment(prmPaymentInfo);
    }
    @PostMapping("/invoices/{invoiceNumber}/confirmOrReject")
    public ResponseEntity<?> confirmOrReject(@PathVariable Long invoiceNumber,@Valid @RequestBody ConfirmPaymentDTO prmPaymentInfo){
        return this.invoiceService.confirmOrRejectPaymnt(invoiceNumber,prmPaymentInfo);
    }
    @PostMapping("/invoices/test")
    public ResponseEntity<?> test(@Valid @RequestBody doPaymentDTO prmPaymentInfo){
        return this.invoiceService.testPayment(prmPaymentInfo);
    }
}
