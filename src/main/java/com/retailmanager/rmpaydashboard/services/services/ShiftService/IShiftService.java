package com.retailmanager.rmpaydashboard.services.services.ShiftService;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.ShiftDTO;

public interface IShiftService {
    public ResponseEntity<?> createShift(ShiftDTO shiftDTO);
    public ResponseEntity<?> updateShift(ShiftDTO shiftDTO);
    public ResponseEntity<?> deleteShift(Long shiftId);
    public ResponseEntity<?> closeShift(ShiftDTO shiftDTO);
    public ResponseEntity<?> getShiftById(Long shiftId);
    public ResponseEntity<?> getAllShifts(Long businessId,Long employeeId,
        String serialNumber,
        String startDate,
        String endDate,
        Boolean statusShiftBalance,
        Pageable pageable);
    

}
