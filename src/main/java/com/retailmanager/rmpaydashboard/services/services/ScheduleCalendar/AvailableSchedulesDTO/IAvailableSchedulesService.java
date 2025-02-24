package com.retailmanager.rmpaydashboard.services.services.ScheduleCalendar.AvailableSchedulesDTO;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.AvailableSchedulesDTO;

public interface IAvailableSchedulesService {
    public ResponseEntity<?> save(AvailableSchedulesDTO prmService);
    public ResponseEntity<?> update(Long asId, AvailableSchedulesDTO prmService);
    public boolean delete(Long asId);
    public ResponseEntity<?> findById(Long serviceId);
    public ResponseEntity<?> getAll(Long employeeId);
    public ResponseEntity<?> getAllByBusinessId(Long prmBusinessId);
}
