package com.retailmanager.rmpayCalendar.services.services.ScheduleCalendar.StaffingRequirement;

import com.retailmanager.rmpayCalendar.services.DTO.StaffingRequirementDTO;
import org.springframework.http.ResponseEntity;

public interface IStaffingRequirementService {
    ResponseEntity<?> save(StaffingRequirementDTO dto);
    ResponseEntity<?> update(Long id, StaffingRequirementDTO dto);
    boolean delete(Long id);
    ResponseEntity<?> findById(Long id);
    ResponseEntity<?> getAll();
}
