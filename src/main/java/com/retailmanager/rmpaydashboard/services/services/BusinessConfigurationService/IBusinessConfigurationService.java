package com.retailmanager.rmpaydashboard.services.services.BusinessConfigurationService;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.BusinessConfigurationDTO;
import com.retailmanager.rmpaydashboard.services.DTO.BusinessConfigurationMini;

public interface IBusinessConfigurationService {
    public ResponseEntity<?> create(BusinessConfigurationDTO config);

    // Get a BusinessConfiguration by its ID
    public ResponseEntity<?> getById(Long id);

    // Get all BusinessConfigurations
    public ResponseEntity<?> getAll();
    public ResponseEntity<?> getAllByKey(String configKey, Long businessId);

    // Update an existing BusinessConfiguration
    public ResponseEntity<?> update(Long configurationId, BusinessConfigurationDTO config);
    public ResponseEntity<?> update(Long businessId, List<BusinessConfigurationMini> config);

    // Delete a BusinessConfiguration by its ID
    public ResponseEntity<?> delete(Long id);
}
