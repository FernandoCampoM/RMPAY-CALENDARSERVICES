package com.retailmanager.rmpaydashboard.services.services.RMPayAtTheTable.TerminalService;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.RMPayAtTheTable_TerminalDTO;

public interface ITerminalPayAtTableService {
    ResponseEntity<?> createTerminal(RMPayAtTheTable_TerminalDTO terminalDTO);
    ResponseEntity<?> updateTerminal(Long terminalId, RMPayAtTheTable_TerminalDTO terminalDTO);
    ResponseEntity<?> deleteTerminal(Long terminalId);
    ResponseEntity<?> deleteTerminal(String serialNumber);
    ResponseEntity<RMPayAtTheTable_TerminalDTO> getTerminalById(Long terminalId);
    ResponseEntity<RMPayAtTheTable_TerminalDTO> getTerminalBySerialNumber(String SerialNumber);
    ResponseEntity<List<RMPayAtTheTable_TerminalDTO>> getAllTerminals();
    ResponseEntity<List<RMPayAtTheTable_TerminalDTO>> getTerminalsByUserId(Long userId);
    ResponseEntity<?> activateTerminal(Long terminalId);
    ResponseEntity<?> deactivateTerminal(Long terminalId);
}
