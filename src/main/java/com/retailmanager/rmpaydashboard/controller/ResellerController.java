package com.retailmanager.rmpaydashboard.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.retailmanager.rmpaydashboard.services.DTO.ResellerDTO;
import com.retailmanager.rmpaydashboard.services.DTO.ResellerPaymentDTO;
import com.retailmanager.rmpaydashboard.services.services.ResellerServices.IResellerService;

@RestController
@RequestMapping("/api")
public class ResellerController {

    @Autowired
    private IResellerService resellerServices;

    @PostMapping("/resellers")
    public ResponseEntity<?> createReseller(@RequestBody ResellerDTO prmReseller) {
        return resellerServices.save(prmReseller);
    }

    @PutMapping("/resellers/{id}")
    public ResponseEntity<?> updateReseller(@PathVariable Long id, @RequestBody ResellerDTO prmReseller) {
        return resellerServices.update(prmReseller, id);
    }

    @GetMapping("/resellers/{id}")
    public ResponseEntity<?> getResellerById(@PathVariable Long id) {
        return resellerServices.get(id);
    }

    @GetMapping("/resellers")
    public ResponseEntity<?> getAllResellers() {
        return resellerServices.getAll();
    }

    @PutMapping("/resellers/{id}/status/{status}")
    public ResponseEntity<?> updateResellerStatus(@PathVariable Long id, @PathVariable boolean status) {
        return resellerServices.updateStatus(id, status);
    }

    @DeleteMapping("/resellers/{id}")
    public ResponseEntity<?> deleteReseller(@PathVariable Long id) {
        return resellerServices.delete(id);
    }

    @GetMapping("/resellers/{id}/exists")
    public ResponseEntity<?> resellerExists(@PathVariable Long id) {
        System.out.println("id--------------------------: " + id);
        return resellerServices.exist(id);
    }

    @GetMapping("/resellers/username/{username}")
    public ResponseEntity<?> findResellerByUsername(@PathVariable String username) {
        return resellerServices.findByUsername(username);
    }

    /**
     * Retrieves the accounts sold by a specific reseller.
     *
     * @param prmResellerId The ID of the reseller.
     * @return The accounts sold by the reseller.
     */
    @GetMapping("/resellers/{prmResellerId}/accounts-sold")
    public ResponseEntity<?> getAccountsSold(@PathVariable Long prmResellerId) {
        return resellerServices.getAccountsSold(prmResellerId);
    }

    @GetMapping("/resellers/{id}/qrcode")
    public ResponseEntity<?> getQRCode(@PathVariable Long id) {
        return resellerServices.getQRcode(id);
    }

    @GetMapping("/resellers/accounts-report")
    public ResponseEntity<?> getAccountsReport() {
        return resellerServices.getAccountsReport();
    }

    @GetMapping("/resellers/{id}/accounts-report")
    public ResponseEntity<?> getAccountsReport(@PathVariable Long id) {
        return resellerServices.getAccountsReport(id);
    }

    @GetMapping("/resellers/{id}/unpaid-accounts")
    public ResponseEntity<?> getUnpaidAccounts(@PathVariable Long id) {
        return resellerServices.getUnpaidAccounts(id);
    }

    @PostMapping("/resellers/payments")
    public ResponseEntity<?> doPayment(@RequestBody ResellerPaymentDTO prmPayment) {
        return resellerServices.doPayment(prmPayment);
    }
    @GetMapping("/resellers/{prmResellerId}/payments")
    public ResponseEntity<?> getPaymentHistory(@PathVariable Long prmResellerId) {
        return resellerServices.getPaymentHistory(prmResellerId);
    }
}

