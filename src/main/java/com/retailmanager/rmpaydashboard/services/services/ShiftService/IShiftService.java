package com.retailmanager.rmpaydashboard.services.services.ShiftService;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.ShiftDTO;

public interface IShiftService {
    public ResponseEntity<?> openShift(String authToken, ShiftDTO shiftDTO);
}
