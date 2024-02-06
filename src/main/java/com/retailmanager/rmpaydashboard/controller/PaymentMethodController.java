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

import com.retailmanager.rmpaydashboard.services.DTO.PaymentMethodDTO;
import com.retailmanager.rmpaydashboard.services.services.PaymentMethodService.IPaymentMethosService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api")
@Validated
public class PaymentMethodController {

    @Autowired
    private IPaymentMethosService paymentMethodsService;

    /**
     * Save a payment method.
     *
     * @param  prmPaymentMethod   the PaymentMethodDTO to be saved
     * @return                    a ResponseEntity containing the saved payment method
     */
    @PostMapping("/payment-methods")
    public ResponseEntity<?> save(@Valid @RequestBody PaymentMethodDTO prmPaymentMethod) {
        return paymentMethodsService.save(prmPaymentMethod);
    }

    /**
     * Updates a payment method.
     *
     * @param  code              the code of the payment method
     * @param  prmPaymentMethod  the payment method DTO to update
     * @return                   the response entity
     */
    @PutMapping("/payment-methods/{code}")
    public ResponseEntity<?> update(@PathVariable @NotBlank(message = "code.notBlank") String code,
            @Valid @RequestBody PaymentMethodDTO prmPaymentMethod) {
        return paymentMethodsService.update(code, prmPaymentMethod);
    }

    /**
     * Deletes a payment method by its code.
     *
     * @param  code   the code of the payment method to be deleted
     * @return       true if the payment method is successfully deleted, false otherwise
     */
    @DeleteMapping("/payment-methods/{code}")
    public boolean delete(@PathVariable @NotBlank(message = "code.notBlank") String code) {
        return paymentMethodsService.delete(code);
    }

    /**
     * Finds payment methods by code.
     *
     * @param  code  The code of the payment method
     * @return       The ResponseEntity containing the result of the search
     */
    @GetMapping("/payment-methods/{code}")
    public ResponseEntity<?> findByCode(@PathVariable @NotBlank(message = "code.notBlank") String code) {
        return paymentMethodsService.findByCode(code);
    }

    /**
     * Find payment methods by name.
     *
     * @param  name   The name of the payment method to find
     * @return       The ResponseEntity containing the result of the find operation
     */
    @GetMapping("/payment-methods/findByName")
    public ResponseEntity<?> findByName(@RequestParam(name = "name") @NotBlank(message = "name.notBlank") String name) {
        return paymentMethodsService.findByName(name);
    }

    /**
     * Update the enable status for a payment method.
     *
     * @param  code   the code of the payment method to update
     * @param  enable the new enable status
     * @return        the response entity
     */
    @PutMapping("/payment-methods/{code}/enable/{enable}")
    public ResponseEntity<?> updateEnable(@PathVariable @NotBlank(message = "code.notBlank") String code,
            @PathVariable boolean enable) {
        return paymentMethodsService.updateEnable(code, enable);
    }

    /**
     * Retrieves all payment methods.
     *
     * @return         the ResponseEntity containing the result of finding all payment methods
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<?> findAll() {
        return paymentMethodsService.findAll();
    }

    /**
     * Get all active payment methods.
     *
     * @return ResponseEntity with all active payment methods
     */
    @GetMapping("/payment-methods/actives")
    public ResponseEntity<?> findAllActives() {
        return paymentMethodsService.findAllActives();
    }
}
