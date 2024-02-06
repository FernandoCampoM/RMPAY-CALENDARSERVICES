package com.retailmanager.rmpaydashboard.services.services.PaymentMethodService;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.PaymentMethodDTO;

public interface IPaymentMethosService {
    public ResponseEntity<?> save(PaymentMethodDTO prmPaymentMethod);
    public ResponseEntity<?> update(String code, PaymentMethodDTO prmPaymentMethod);
    public boolean delete(String code);
    public ResponseEntity<?> findByCode(String code);
    public ResponseEntity<?> findAll();
    public ResponseEntity<?> findAllActives();
    public ResponseEntity<?> findByName(String name);
    public ResponseEntity<?> updateEnable(String code, boolean enable);
}
