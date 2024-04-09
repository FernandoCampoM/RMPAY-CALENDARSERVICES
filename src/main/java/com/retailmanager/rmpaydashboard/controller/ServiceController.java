package com.retailmanager.rmpaydashboard.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.services.DTO.ServiceDTO;
import com.retailmanager.rmpaydashboard.services.services.EmailService.IEmailService;
import com.retailmanager.rmpaydashboard.services.services.ServiceService.IServiceService;

@RestController
@RequestMapping("/api")
@Validated



public class ServiceController {
    @Autowired
    private IEmailService emailService;
    @Autowired
    private IServiceService serviceService;

    /**
     * Save a service using the provided ServiceDTO.
     *
     * @param  prmService   the ServiceDTO to be saved
     * @return              the ResponseEntity containing the result of the save operation
     */
    @PostMapping("/services")
    
    public ResponseEntity<?> save(@Valid @RequestBody ServiceDTO prmService) {
        return serviceService.save(prmService);
    }

    /**
     * Update a service by ID.
     *
     * @param  serviceId   the ID of the service to update
     * @param  prmService  the ServiceDTO object containing updated information
     * @return             a ResponseEntity with the updated service
     */
    @PutMapping("/services/{serviceId}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "serviceId.positive") Long serviceId,
            @Valid @RequestBody ServiceDTO prmService) {
        return serviceService.update(serviceId, prmService);
    }

    /**
     * Deletes a service by ID.
     *
     * @param  serviceId   the ID of the service to delete
     * @return             true if the service is successfully deleted, false otherwise
     */
    @DeleteMapping("/services/{serviceId}")
    public boolean delete(@Valid @PathVariable @Positive(message = "serviceId.positive") Long serviceId) {
        return serviceService.delete(serviceId);
    }

    /**
     * Find service by ID.
     *
     * @param  serviceId	positive service ID
     * @return         	response entity with the found service
     */
    @GetMapping("/services/{serviceId}")
    
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "serviceId.positive") Long serviceId) {
        return serviceService.findById(serviceId);
    }

    /**
     * Get all services.
     *
     * @return         response entity with all services
     */
    @GetMapping("/services")
    public ResponseEntity<?> getAll() {
        return serviceService.getAll();
    }
    /**
     * Get the active services
     *
     * @return         the ResponseEntity containing the active services
     */
    @GetMapping("/services/actives")
    public ResponseEntity<?> getActivesServices() {
        return serviceService.getActivesServices();
    }


    /**
     * Update the enable status of a service.
     *
     * @param  serviceId  the ID of the service to update
     * @param  enable     the new enable status
     * @return            the ResponseEntity representing the result of the update
     */
    @PutMapping("/services/{serviceId}/enable/{enable}")
    public ResponseEntity<?> updateEnable(@Valid @PathVariable @Positive(message = "serviceId.positive") Long serviceId,
            @Valid @PathVariable boolean enable) {
        return serviceService.updateEnable(serviceId, enable);
    }

    @GetMapping("/services/testemail")
    public ResponseEntity<?> testEmail() {

        List<String> toList = Arrays.asList("juancamm@unicauca.edu.co", "juancampo201509@gmail.com");
        List<String> cc = Arrays.asList("naslybecoche.q@gmail.com");
        String subject = "Correo HTML con adjunto y CCO de prueba";
        String htmlBody = "<html><body><h1>Hola,</h1><p>Este es un correo HTML con adjunto y CCO enviado desde Spring Boot.</p></body></html>";

        // Datos del archivo adjunto
        byte[] attachmentData = obtenerDatosAdjunto(); // Implementa tu lógica para obtener los datos del archivo
        String attachmentFileName = "adjunto.txt";

        emailService.sendHtmlEmailWithAttachmentAndCCO(toList, subject, htmlBody, cc, attachmentData, attachmentFileName);

        return new ResponseEntity<>("Email enviado correctamente", org.springframework.http.HttpStatus.OK);
        
    }
    private byte[] obtenerDatosAdjunto() {
        // Implementa la lógica para obtener los datos del archivo adjunto
        // En este ejemplo, simplemente retornamos un archivo de texto con contenido de prueba.
        return "Contenido del archivo adjunto de prueba.".getBytes();
    }
}
