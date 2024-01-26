package com.retailmanager.rmpaydashboard.controller;


import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.services.DTO.ServiceDTO;
import com.retailmanager.rmpaydashboard.services.services.ServiceService.IServiceService;

@RestController
@RequestMapping("/api")
@Validated
@PermitAll  //Permit that all methods are available without authentication
public class ServiceController {

    @Autowired
    private IServiceService serviceService;

    /**
     * Save a service using the provided ServiceDTO.
     *
     * @param  prmService   the ServiceDTO to be saved
     * @return              the ResponseEntity containing the result of the save operation
     */
    @PostMapping("/services")
    public ResponseEntity<?> save(@Valid @RequestBody ServiceDTO prmService) {
        return serviceService.save(prmService);
    }

    /**
     * Update a service by ID.
     *
     * @param  serviceId   the ID of the service to update
     * @param  prmService  the ServiceDTO object containing updated information
     * @return             a ResponseEntity with the updated service
     */
    @PutMapping("/services/{serviceId}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "serviceId.positive") Long serviceId,
            @Valid @RequestBody ServiceDTO prmService) {
        return serviceService.update(serviceId, prmService);
    }

    /**
     * Deletes a service by ID.
     *
     * @param  serviceId   the ID of the service to delete
     * @return             true if the service is successfully deleted, false otherwise
     */
    @DeleteMapping("/services/{serviceId}")
    public boolean delete(@Valid @PathVariable @Positive(message = "serviceId.positive") Long serviceId) {
        return serviceService.delete(serviceId);
    }

    /**
     * Find service by ID.
     *
     * @param  serviceId	positive service ID
     * @return         	response entity with the found service
     */
    @GetMapping("/services/{serviceId}")
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "serviceId.positive") Long serviceId) {
        return serviceService.findById(serviceId);
    }

    /**
     * Get all services.
     *
     * @return         response entity with all services
     */
    @GetMapping("/services")
    public ResponseEntity<?> getAll() {
        return serviceService.getAll();
    }

    /**
     * Update the enable status of a service.
     *
     * @param  serviceId  the ID of the service to update
     * @param  enable     the new enable status
     * @return            the ResponseEntity representing the result of the update
     */
    @PostMapping("/services/{serviceId}/enable/{enable}")
    public ResponseEntity<?> updateEnable(@Valid @PathVariable @Positive(message = "serviceId.positive") Long serviceId,
            @Valid @PathVariable boolean enable) {
        return serviceService.updateEnable(serviceId, enable);
    }
}
