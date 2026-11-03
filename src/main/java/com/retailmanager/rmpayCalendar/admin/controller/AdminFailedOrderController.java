package com.retailmanager.rmpayCalendar.admin.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpayCalendar.admin.service.AuditService;
import com.retailmanager.rmpayCalendar.db2.entity.FailedOrder;
import com.retailmanager.rmpayCalendar.db2.repository.FailedOrderRepository;
import com.retailmanager.rmpayCalendar.services.services.Shopify.InvoiceSyncService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Facturas de pedidos de Shopify que no llegaron a RM (tabla FailedOrder):
 * listado y reintento manual. Exige sesión del panel como el resto de /admin/api.
 * Depende de InvoiceSyncService.retryFailedOrder.
 */
@RestController
@RequestMapping("/admin/api")
public class AdminFailedOrderController {

    @Autowired
    private FailedOrderRepository failedOrderRepository;
    @Autowired
    private InvoiceSyncService invoiceSyncService;
    @Autowired
    private AuditService audit;

    @GetMapping("/failed-orders")
    public Map<String, Object> list(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page) {
        Page<FailedOrder> result = failedOrderRepository.search(AdminControllerSupport.like(q),
                PageRequest.of(Math.max(page, 0), 50, Sort.by(Sort.Direction.DESC, "lastAttemptAt")));
        List<Map<String, Object>> rows = result.getContent().stream().map(f -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", f.getId());
            m.put("orderName", f.getOrderName());
            m.put("attempts", f.getAttempts());
            m.put("maxAttempts", InvoiceSyncService.MAX_FAILED_ATTEMPTS);
            m.put("lastError", f.getLastError());
            m.put("firstFailedAt", f.getFirstFailedAt() == null ? null : f.getFirstFailedAt().toString());
            m.put("lastAttemptAt", f.getLastAttemptAt() == null ? null : f.getLastAttemptAt().toString());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("rows", rows);
        map.put("total", result.getTotalElements());
        map.put("page", result.getNumber());
        map.put("pages", result.getTotalPages());
        map.put("dryRun", invoiceSyncService.isDryRun());
        return map;
    }

    /** Reintenta ya una factura fallida, aunque haya llegado al máximo de intentos. */
    @PostMapping("/failed-orders/retry")
    public Map<String, Object> retry(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (invoiceSyncService.isDryRun()) {
            throw new IllegalStateException("La simulación está activa (shopify.invoice_dry_run): no se envían facturas");
        }
        String id = String.valueOf(body.get("id"));
        FailedOrder failed = failedOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la factura fallida " + id));

        boolean sent = invoiceSyncService.retryFailedOrder(failed);
        audit.log(AdminControllerSupport.user(request), "REINTENTAR_FACTURA",
                id + (sent ? " | enviada" : " | falló de nuevo"));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sent", sent);
        failedOrderRepository.findById(id).ifPresent(f -> map.put("lastError", f.getLastError()));
        return map;
    }
}
