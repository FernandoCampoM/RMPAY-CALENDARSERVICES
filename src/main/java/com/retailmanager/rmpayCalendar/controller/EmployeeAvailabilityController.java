package com.retailmanager.rmpayCalendar.controller;

import com.retailmanager.rmpayCalendar.services.DTO.EmployeeAvailabilityDTO;
import com.retailmanager.rmpayCalendar.services.services.ScheduleCalendar.EmployeeAvailability.IEmployeeAvailabilityService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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

@RestController
@RequestMapping("/api/employeeAvailability")
@Validated
public class EmployeeAvailabilityController {

    @Autowired
    private IEmployeeAvailabilityService service;

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody EmployeeAvailabilityDTO dto) {
        return service.save(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable @Positive Long id,
                                    @Valid @RequestBody EmployeeAvailabilityDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable @Positive Long id) {
        return service.delete(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable @Positive Long id) {
        return service.findById(id);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(name = "employeeId", required = false) Long employeeId) {
        if (employeeId != null) return service.getByEmployeeId(employeeId);
        return service.getAll();
    }
}
