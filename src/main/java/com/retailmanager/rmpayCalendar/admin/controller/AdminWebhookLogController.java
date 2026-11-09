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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpayCalendar.db2.entity.WebhookLog;
import com.retailmanager.rmpayCalendar.db2.repository.WebhookLogRepository;

/**
 * Traza de los webhooks de Shopify (tabla WebhookLog) para el panel: listado y detalle.
 * Exige sesión del panel como el resto de /admin/api. Solo lectura.
 */
@RestController
@RequestMapping("/admin/api")
public class AdminWebhookLogController {

    @Autowired
    private WebhookLogRepository webhookLogRepository;

    @GetMapping("/webhooks")
    public Map<String, Object> list(@RequestParam(required = false) String q,
                                    @RequestParam(defaultValue = "0") int page) {
        Page<WebhookLog> rows = webhookLogRepository.search(AdminControllerSupport.like(q),
                PageRequest.of(Math.max(page, 0), 50, Sort.by(Sort.Direction.DESC, "receivedAt")));
        List<Map<String, Object>> list = rows.getContent().stream().map(w -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", w.getId());
            m.put("receivedAt", w.getReceivedAt() == null ? null : w.getReceivedAt().toString());
            m.put("endpoint", w.getEndpoint());
            m.put("topic", w.getTopic());
            m.put("shopifyOrderId", w.getShopifyOrderId());
            m.put("orderName", w.getOrderName());
            m.put("result", w.getResult());
            m.put("httpStatus", w.getHttpStatus());
            m.put("durationMs", w.getDurationMs());
            m.put("error", w.getError());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("rows", list);
        map.put("total", rows.getTotalElements());
        map.put("page", rows.getNumber());
        map.put("pages", rows.getTotalPages());
        return map;
    }

    @GetMapping("/webhooks/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        WebhookLog w = webhookLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe el registro de webhook " + id));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", w.getId());
        m.put("receivedAt", w.getReceivedAt() == null ? null : w.getReceivedAt().toString());
        m.put("endpoint", w.getEndpoint());
        m.put("topic", w.getTopic());
        m.put("webhookId", w.getWebhookId());
        m.put("shopDomain", w.getShopDomain());
        m.put("hmacPresent", w.getHmacPresent());
        m.put("hmacValid", w.getHmacValid());
        m.put("shopifyOrderId", w.getShopifyOrderId());
        m.put("orderName", w.getOrderName());
        m.put("result", w.getResult());
        m.put("detail", w.getDetail());
        m.put("httpStatus", w.getHttpStatus());
        m.put("durationMs", w.getDurationMs());
        m.put("payload", w.getPayload());
        m.put("invoiceJson", w.getInvoiceJson());
        m.put("responseBody", w.getResponseBody());
        m.put("error", w.getError());
        m.put("stackTrace", w.getStackTrace());
        return m;
    }
}
