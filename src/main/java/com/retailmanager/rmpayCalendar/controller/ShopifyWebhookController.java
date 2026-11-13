package com.retailmanager.rmpayCalendar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpayCalendar.services.services.Shopify.InvoiceSyncService;

@RestController
@RequestMapping("/webhook")
public class ShopifyWebhookController {
    @Autowired
    private InvoiceSyncService invoiceSyncService;

    @PostMapping("/orders")
    public ResponseEntity<?> receiveOrder(
            @RequestHeader("X-Shopify-Hmac-Sha256") String hmac,
            @RequestBody String payload) {
                System.out.println("📦 Payload recibido: " + payload);
                System.out.println("📦 HMAC recibido: " + hmac);
       return invoiceSyncService.receiveOrder(hmac, payload);
       
    }
    @PostMapping("/orders/cancelled")
    public ResponseEntity<?> receiveCancelledOrder(
            @RequestHeader("X-Shopify-Hmac-Sha256") String hmac,
            @RequestBody String payload) {
                System.out.println("📦 Payload recibido: " + payload);
                System.out.println("📦 HMAC recibido: " + hmac);
       return invoiceSyncService.receiveCancelledOrder(hmac, payload);
       
    }
    @PostMapping("/refunds")
    public ResponseEntity<?> receiveRefund(
            @RequestHeader("X-Shopify-Hmac-Sha256") String hmac,
            @RequestBody String payload) {
                System.out.println("📦 Payload recibido: " + payload);
                System.out.println("📦 HMAC recibido: " + hmac);
       return invoiceSyncService.receiveRefund(hmac, payload);
       
    }
}