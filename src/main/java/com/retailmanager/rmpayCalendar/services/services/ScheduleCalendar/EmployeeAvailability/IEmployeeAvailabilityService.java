package com.retailmanager.rmpayCalendar.services.services.ScheduleCalendar.EmployeeAvailability;

import com.retailmanager.rmpayCalendar.services.DTO.EmployeeAvailabilityDTO;
import org.springframework.http.ResponseEntity;

public interface IEmployeeAvailabilityService {
    ResponseEntity<?> save(EmployeeAvailabilityDTO dto);
    ResponseEntity<?> update(Long id, EmployeeAvailabilityDTO dto);
    boolean delete(Long id);
    ResponseEntity<?> findById(Long id);
    ResponseEntity<?> getAll();
    ResponseEntity<?> getByEmployeeId(Long employeeId);
}
