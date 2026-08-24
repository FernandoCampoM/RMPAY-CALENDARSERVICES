package com.retailmanager.rmpayCalendar.services.services.ScheduleCalendar;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpayCalendar.models.ScheduleCalendar;
import com.retailmanager.rmpayCalendar.services.DTO.ScheduleCalendarDTO;

public interface IScheduleCalendarService {
    public ResponseEntity<?> save(ScheduleCalendarDTO prmService);
    public ResponseEntity<?> update(Long id, ScheduleCalendar prmService);
    public boolean delete(Long id);
    public ResponseEntity<?> findById(Long serviceId);
    public ResponseEntity<?> getAll(Long employeeId);
    public ResponseEntity<?> getAllByBusinessId(Long prmBusinessId);

    
}
