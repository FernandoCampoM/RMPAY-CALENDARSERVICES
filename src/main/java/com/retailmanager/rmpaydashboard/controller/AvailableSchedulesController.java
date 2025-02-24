package com.retailmanager.rmpaydashboard.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.services.DTO.AvailableSchedulesDTO;
import com.retailmanager.rmpaydashboard.services.services.ScheduleCalendar.AvailableSchedulesDTO.IAvailableSchedulesService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/availableSchedules")
@Validated
public class AvailableSchedulesController {

    @Autowired
    private IAvailableSchedulesService availableSchedulesService;

    /**
     * Save an available schedule.
     *
     * @param prmService the AvailableSchedulesDTO to be saved
     * @return the ResponseEntity containing the result of the save operation
     */
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody AvailableSchedulesDTO prmService) {
        return availableSchedulesService.save(prmService);
    }

    /**
     * Update an available schedule by ID.
     *
     * @param id         the ID of the available schedule to update
     * @param prmService the AvailableSchedulesDTO object containing updated information
     * @return a ResponseEntity with the updated available schedule
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "asId.positive") Long id,
                                     @Valid @RequestBody AvailableSchedulesDTO prmService) {
        return availableSchedulesService.update(id, prmService);
    }

    /**
     * Deletes an available schedule by ID.
     *
     * @param id the ID of the available schedule to delete
     * @return true if the available schedule is successfully deleted, false otherwise
     */
    @DeleteMapping("/{id}")
    public boolean delete(@Valid @PathVariable @Positive(message = "asId.positive") Long id) {
        return availableSchedulesService.delete(id);
    }

    /**
     * Find an available schedule by ID.
     *
     * @param id the ID of the available schedule to find
     * @return the ResponseEntity containing the found available schedule
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "asId.positive") Long id) {
        return availableSchedulesService.findById(id);
    }

    /**
     * Get all available schedules for an employee or business.
     *
     * @param employeeId the ID of the employee
     * @param businessId the ID of the business
     * @return the ResponseEntity containing all available schedules for the specified employee or business
     */
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(name = "employeeId", required = false) @Positive(message = "employeeId.positive") Long employeeId,
                                     @RequestParam(name = "businessId", required = false) @Positive(message = "businessId.positive") Long businessId) {
        if (businessId != null) {
            return availableSchedulesService.getAllByBusinessId(businessId);
        }
        if (employeeId != null) {
            return availableSchedulesService.getAll(employeeId);
        }
        return ResponseEntity.badRequest().body("Either employeeId or businessId must be provided.");
    }
}
