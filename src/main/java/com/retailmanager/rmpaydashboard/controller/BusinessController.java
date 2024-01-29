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
import com.retailmanager.rmpaydashboard.services.DTO.BusinessDTO;
import com.retailmanager.rmpaydashboard.services.services.BusinessService.IBusinessService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api")
@Validated
public class BusinessController {
    @Autowired
    private IBusinessService businessService;
    /**
     * Find a business by ID.
     *
     * @param  businessId   the ID of the business to find
     * @return              the ResponseEntity containing the business found
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<?> findById(@Valid @PathVariable @Positive(message = "El id del negocio debe ser positivo")Long businessId){
        return this.businessService.findById(businessId);
    }
    /**
     * Find business by merchant ID.
     *
     * @param  merchantId  The merchant ID to search for
     * @return             The response entity with the result
     */
    @GetMapping("/business")
    public ResponseEntity<?> findByMerchantId(@Valid @RequestParam(name = "merchantId") @NotBlank(message = "El merchantId no puede estar vacío") String merchantId){
        return this.businessService.findByMerchantId(merchantId);
    }
    /**
     * Save a business entity using the provided BusinessDTO.
     *
     * @param  prmBusiness   the BusinessDTO to be saved
     * @return               a ResponseEntity containing the result of the save operation
     */
    @PostMapping("/business")
    public ResponseEntity<?> save(@Valid @RequestBody BusinessDTO prmBusiness){
        return this.businessService.save(prmBusiness);
    }
    /**
     * Update business information by ID.
     *
     * @param  businessId       the ID of the business to update
     * @param  prmBusinessDTO   the DTO containing the updated business information
     * @return                  the ResponseEntity containing the result of the update operation
     */
    @PutMapping("/business/{businessId}")
    public ResponseEntity<?> update(@Valid @PathVariable @Positive(message = "El id del negocio debe ser positivo")Long businessId,@Valid @RequestBody BusinessDTO prmBusinessDTO){
        return this.businessService.update(businessId,prmBusinessDTO);
    }
    /**
     * Delete a business by ID.
     *
     * @param  businessId   The ID of the business to be deleted
     * @return              True if the business is successfully deleted, false otherwise
     */
    @DeleteMapping("/business/{businessId}")
    public Boolean delete(@Valid @PathVariable @Positive(message = "El id del negocio debe ser positivo")Long businessId){
        return this.businessService.delete(businessId);
    }
    /**
     * Get terminals for a specific business.
     *
     * @param  businessId   The ID of the business
     * @return              ResponseEntity containing the terminals for the specified business
     */
    @GetMapping("/business/{businessId}/terminals")
    public ResponseEntity<?> getTerminals(@Valid @PathVariable @Positive(message = "El id del negocio debe ser positivo")Long businessId){
        return this.businessService.getTerminals(businessId);
    }
    /**
     * Get categories for a specific business.
     *
     * @param  businessId   the ID of the business
     * @return              the ResponseEntity containing the categories
     */
    @GetMapping("/business/{businessId}/categories")
    public ResponseEntity<?> getCategories(@Valid @PathVariable @Positive(message = "El id del negocio debe ser positivo")Long businessId){
        return this.businessService.getCategories(businessId);
    }
    /**
     * Update the enable status of a business.
     *
     * @param  businessId  The ID of the business to update
     * @param  enable      The new enable status
     * @return             The ResponseEntity containing the result of the update
     */
    @PutMapping("/business/{businessId}/enable/{enable}")
    public ResponseEntity<?> updateEnable(@Valid @PathVariable @Positive(message = "El id del negocio debe ser positivo")Long businessId, @Valid @PathVariable boolean enable){
        return this.businessService.updateEnable(businessId, enable);
    }
}
