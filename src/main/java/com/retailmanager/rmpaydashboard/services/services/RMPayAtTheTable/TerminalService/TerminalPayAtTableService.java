package com.retailmanager.rmpaydashboard.services.services.RMPayAtTheTable.TerminalService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.models.rmpayAtTheTable.RMPayAtTheTable_Terminal;
import com.retailmanager.rmpaydashboard.repositories.TerminalPayAtTableRepository;
import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.RMPayAtTheTable_TerminalDTO;

@Service
public class TerminalPayAtTableService implements ITerminalPayAtTableService {
    @Autowired
    private TerminalPayAtTableRepository terminalRepository;

    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;

    @Override
    public ResponseEntity<RMPayAtTheTable_TerminalDTO> createTerminal(RMPayAtTheTable_TerminalDTO terminalDTO) {
        RMPayAtTheTable_Terminal terminal = mapper.map(terminalDTO, RMPayAtTheTable_Terminal.class);
        RMPayAtTheTable_Terminal savedTerminal = terminalRepository.save(terminal);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(savedTerminal, RMPayAtTheTable_TerminalDTO.class));
    }

    @Override
    public ResponseEntity<RMPayAtTheTable_TerminalDTO> updateTerminal(Long terminalId, RMPayAtTheTable_TerminalDTO terminalDTO) {
        RMPayAtTheTable_Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new EntidadNoExisteException("El terminal con ID " + terminalId + " no existe en la Base de datos"));
        terminal.setActive(terminalDTO.getActive());
        terminal.setLastTransmissionDate(terminalDTO.getLastTransmissionDate());
        terminal.setRegistrationDate(terminalDTO.getRegistrationDate());
        terminal.setSerialNumber(terminalDTO.getSerialNumber());
        RMPayAtTheTable_Terminal updatedTerminal = terminalRepository.save(terminal);
        return ResponseEntity.ok(mapper.map(updatedTerminal, RMPayAtTheTable_TerminalDTO.class));
    }

    @Override
    public ResponseEntity<?> deleteTerminal(Long terminalId) {
        if (!terminalRepository.existsById(terminalId)) {
            throw new EntidadNoExisteException("El terminal con ID " + terminalId + " no existe en la Base de datos");
        }
        try {
            terminalRepository.deleteById(terminalId);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @Override
    public ResponseEntity<RMPayAtTheTable_TerminalDTO> getTerminalById(Long terminalId) {
        RMPayAtTheTable_Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new EntidadNoExisteException("El terminal con ID " + terminalId + " no existe en la Base de datos"));
        return ResponseEntity.ok(terminal.toDTO());
    }

    @Override
    public ResponseEntity<List<RMPayAtTheTable_TerminalDTO>> getAllTerminals() {
        List<RMPayAtTheTable_Terminal> terminals = (List<RMPayAtTheTable_Terminal>) terminalRepository.findAll();
        return ResponseEntity.ok(terminals.stream().map(terminal -> mapper.map(terminal, RMPayAtTheTable_TerminalDTO.class)).collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<List<RMPayAtTheTable_TerminalDTO>> getTerminalsByUserId(Long userId) {
        List<RMPayAtTheTable_Terminal> terminals = terminalRepository.findByUserId(userId);
        if (terminals.isEmpty()) {
            throw new EntidadNoExisteException("No existen terminales asociados al usuario con ID " + userId);
        }
        return ResponseEntity.ok(terminals.stream().map(terminal -> terminal.toDTO()).collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<?> activateTerminal(Long terminalId) {
        RMPayAtTheTable_Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new EntidadNoExisteException("El terminal con ID " + terminalId + " no existe en la Base de datos"));
        
        try {
            terminal.setActive(true);
        terminalRepository.save(terminal);
        return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @Override
    public ResponseEntity<?> deactivateTerminal(Long terminalId) {
        RMPayAtTheTable_Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new EntidadNoExisteException("El terminal con ID " + terminalId + " no existe en la Base de datos"));
        try {
            terminal.setActive(false);
            terminalRepository.save(terminal);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @Override
    public ResponseEntity<?> deleteTerminal(String serialNumber) {
        Optional<RMPayAtTheTable_Terminal> terminal = terminalRepository.findBySerialNumber(serialNumber);
        if (!terminal.isPresent()) {
            throw new EntidadNoExisteException("El terminal con serialNumber " + serialNumber + " no existe en la Base de datos");
        }
        try {
            terminalRepository.deleteById(terminal.get().getTerminalId());
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @Override
    public ResponseEntity<RMPayAtTheTable_TerminalDTO> getTerminalBySerialNumber(String SerialNumber) {
        RMPayAtTheTable_Terminal terminal = terminalRepository.findBySerialNumber(SerialNumber)
        .orElseThrow(() -> new EntidadNoExisteException("El terminal con serialNumber " + SerialNumber + " no existe en la Base de datos"));
return ResponseEntity.ok(terminal.toDTO());}
}
