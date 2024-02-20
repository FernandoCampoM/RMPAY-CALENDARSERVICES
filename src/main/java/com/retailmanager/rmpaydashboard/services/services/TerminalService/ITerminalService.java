package com.retailmanager.rmpaydashboard.services.services.TerminalService;

import org.springframework.http.ResponseEntity;

import com.retailmanager.rmpaydashboard.services.DTO.TerminalDTO;


public interface ITerminalService {
    public ResponseEntity<?> save(TerminalDTO prmTerminal);
    public ResponseEntity<?> buyTerminal(TerminalDTO prmTerminal);
    public ResponseEntity<?> update(Long terminalId, TerminalDTO prmTerminal);
    public boolean delete(Long terminalId);
    public ResponseEntity<?> findById(Long terminalId);
    public ResponseEntity<?> findBySerial(String  serial);
    public ResponseEntity<?> updateEnable(Long terminalId, boolean enable);
}
