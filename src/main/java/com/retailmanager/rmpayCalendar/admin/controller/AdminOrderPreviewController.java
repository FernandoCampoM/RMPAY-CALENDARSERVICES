package com.retailmanager.rmpayCalendar.admin.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpayCalendar.services.services.Shopify.InvoiceSyncService;

/**
 * Vista previa de la factura que se enviaría a RM para un pedido de Shopify. No envía nada.
 * Exige sesión del panel como el resto de /admin/api (ver AdminSecurityFilters).
 * Depende de InvoiceSyncService.previewInvoice.
 */
@RestController
@RequestMapping("/admin/api")
public class AdminOrderPreviewController {

    private static final String ORDER_GID = "gid://shopify/Order/";

    @Autowired
    private InvoiceSyncService invoiceSyncService;

    /** @param id ID de Shopify: numérico (123456) o completo (gid://shopify/Order/123456) */
    @GetMapping("/orders/preview")
    public Map<String, Object> preview(@RequestParam String id) throws Exception {
        String orderId = id.trim();
        if (orderId.isEmpty()) {
            throw new IllegalArgumentException("Falta el ID de la orden");
        }
        if (!orderId.startsWith(ORDER_GID)) {
            orderId = ORDER_GID + orderId;
        }
        return invoiceSyncService.previewInvoice(orderId);
    }
}
