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

       return invoiceSyncService.receiveOrder(hmac, payload);
       
    }
}