package com.retailmanager.rmpaydashboard.services.services.ServiceService;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.ServiceDTO;

public interface IServiceService {
    public ResponseEntity<?> save(ServiceDTO prmService);
    public ResponseEntity<?> update(Long serviceId, ServiceDTO prmService);
    public boolean delete(Long serviceId);
    public ResponseEntity<?> findById(Long serviceId);
    public ResponseEntity<?> getAll();
    public ResponseEntity<?> updateEnable(Long serviceId, boolean enable);
}
