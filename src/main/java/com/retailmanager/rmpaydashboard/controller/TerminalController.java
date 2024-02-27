package com.retailmanager.rmpaydashboard.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpaydashboard.services.DTO.BuyTerminalDTO;
import com.retailmanager.rmpaydashboard.services.DTO.TerminalDTO;
import com.retailmanager.rmpaydashboard.services.services.TerminalService.ITerminalService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api")
@Validated
public class TerminalController {
    @Autowired
    private ITerminalService terminalService;

    /**
     * Find a terminal by ID.
     *
     * @param  terminalId   the ID of the terminal to find
     * @return              the ResponseEntity containing the terminal found
     */
    @GetMapping("/terminals/{terminalId}")
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "El id del terminal debe ser positivo")Long terminalId){
        return this.terminalService.findById(terminalId);
    }
    /**
     * A description of the findBySerial Java function.
     *
     * @param  serial	description of parameter
     * @return         	description of return value
     */
    @GetMapping("/terminals")
    public ResponseEntity<?> findBySerial(@Valid @RequestParam(name = "serial") @NotBlank(message = "El serial del terminal no puede ser vacío") String serial){
        return this.terminalService.findBySerial(serial);
    }
    /**
     * A description of the save function.
     *
     * @param  prmTerminal	description of parameter
     * @return         	description of return value
     */
    @PostMapping("/terminals")
    public ResponseEntity<?> save(@Valid @RequestBody TerminalDTO prmTerminal){
        return this.terminalService.save(prmTerminal);
    }
    @PostMapping("/terminals/buy")
    public ResponseEntity<?> save(@Valid @RequestBody BuyTerminalDTO prmTerminal){
        return this.terminalService.buyTerminal(prmTerminal);
    }
    /**
     * A description of the entire Java function.
     *
     * @param  terminalId  description of parameter
     * @param  prmTerminal  description of parameter
     * @return              description of return value
     */
    @PutMapping("/terminals/{terminalId}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "El id del terminal debe ser positivo")Long terminalId,@Valid @RequestBody TerminalDTO prmTerminal){
        return this.terminalService.update(terminalId,prmTerminal);
    }
    /**
     * Delete a terminal by ID.
     *
     * @param  terminalId   the ID of the terminal to be deleted
     * @return              true if the terminal is successfully deleted, false otherwise
     */
    @DeleteMapping("/terminals/{terminalId}")
    public Boolean delete(@Valid @PathVariable @Positive(message = "El id del terminal debe ser positivo")Long terminalId){
        return this.terminalService.delete(terminalId);
    }
    /**
     * Update the enable status of a terminal.
     *
     * @param  terminalId  the ID of the terminal to update
     * @param  enable      the new enable status
     * @return             the ResponseEntity with the result of the update
     */
    @PutMapping("/terminals/{terminalId}/enable/{enable}")
    public ResponseEntity<?> updateEnable(@Valid @PathVariable @Positive(message = "El id del terminal debe ser positivo")Long terminalId, @Valid @PathVariable boolean enable){
        return this.terminalService.updateEnable(terminalId,enable);
    }
}
