package com.retailmanager.rmpaydashboard.services.services.BusinessService;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.BusinessDTO;
import com.retailmanager.rmpaydashboard.services.DTO.RegsitryBusinessDTO;

public interface IBusinessService {
    public ResponseEntity<?> save(BusinessDTO prmBusiness);
    public ResponseEntity<?> save(RegsitryBusinessDTO prmBusiness);
    public ResponseEntity<?> update(Long businessId, BusinessDTO prmBusiness);
    public boolean delete(Long businessId);
    public ResponseEntity<?> findById(Long businessId);
    public ResponseEntity<?> findByMerchantId(String merchantId);
    public ResponseEntity<?> getTerminals(Long businessId);
    public ResponseEntity<?> getCategories(Long businessId);
    public ResponseEntity<?> updateEnable(Long businessId, boolean enable);
}
