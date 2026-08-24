package com.retailmanager.rmpayCalendar.controller;

import com.retailmanager.rmpayCalendar.services.DTO.StaffingRequirementDTO;
import com.retailmanager.rmpayCalendar.services.services.ScheduleCalendar.StaffingRequirement.IStaffingRequirementService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staffingRequirements")
@Validated
public class StaffingRequirementController {

    @Autowired
    private IStaffingRequirementService service;

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody StaffingRequirementDTO dto) {
        return service.save(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable @Positive Long id,
                                    @Valid @RequestBody StaffingRequirementDTO dto) {
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
    public ResponseEntity<?> getAll() {
        return service.getAll();
    }
}
