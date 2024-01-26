package com.retailmanager.rmpaydashboard.services.services.BusinessService;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.BusinessDTO;

public interface IBusinessService {
    
    public ResponseEntity<?> save(BusinessDTO prmBusiness);
    public ResponseEntity<?> update(Long businessId, BusinessDTO prmBusiness);
    public boolean delete(Long businessId);
    public ResponseEntity<?> findById(Long businessId);
    public ResponseEntity<?> findByMerchantId(String merchantId);
}
