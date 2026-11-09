package com.retailmanager.rmpayCalendar.admin.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpayCalendar.admin.service.AuditService;
import com.retailmanager.rmpayCalendar.db2.entity.WebhookLog;
import com.retailmanager.rmpayCalendar.db2.repository.WebhookLogRepository;
import com.retailmanager.rmpayCalendar.services.services.webhook.WebhookSettingsService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Webhooks de Shopify en el panel: traza (tabla WebhookLog, listado y detalle) y configuración
 * (URL pública, llave de firma y modo de cada webhook, ver WebhookSettingsService).
 * Exige sesión del panel como el resto de /admin/api.
 */
@RestController
@RequestMapping("/admin/api")
public class AdminWebhookLogController {

    @Autowired
    private WebhookLogRepository webhookLogRepository;
    @Autowired
    private WebhookSettingsService webhookSettings;
    @Autowired
    private AuditService audit;

    @Value("${shopify.url:}")
    private String shopifyUrl;

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

    // ───────────── Configuración de los webhooks en Shopify ─────────────

    @GetMapping("/webhooks/config")
    public Map<String, Object> config() {
        String base = webhookSettings.getPublicUrl();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("publicUrl", base);
        map.put("apiVersion", apiVersion());
        map.put("secretSource", webhookSettings.getSecretSource());
        map.put("secretHint", webhookSettings.getSecretHint());
        map.put("webhooks", WebhookSettingsService.ENDPOINTS.stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("key", e.key());
            m.put("label", e.label());
            m.put("topic", e.topic());
            m.put("path", e.path());
            m.put("url", base.isEmpty() ? null : base + e.path());
            m.put("mode", webhookSettings.getMode(e));
            WebhookLog last = webhookLogRepository.findFirstByEndpointOrderByReceivedAtDesc(e.path());
            m.put("lastReceivedAt", last == null || last.getReceivedAt() == null ? null : last.getReceivedAt().toString());
            m.put("lastResult", last == null ? null : last.getResult());
            m.put("lastHmacValid", last == null ? null : last.getHmacValid());
            return m;
        }).collect(Collectors.toList()));
        return map;
    }

    /**
     * Guarda lo que venga en el body: publicUrl, secret (vacío = no cambiar), clearSecret
     * (true = volver a la llave del instalador) y modes ({clave: ACTIVO | SOLO_REGISTRO}).
     * La auditoría registra que cambió la llave, nunca su valor.
     */
    @PutMapping("/webhooks/config")
    public Map<String, Object> saveConfig(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        List<String> changes = new ArrayList<>();
        if (body.containsKey("publicUrl")) {
            String url = String.valueOf(body.get("publicUrl")).trim();
            if (!url.isEmpty() && !url.toLowerCase().startsWith("https://")) {
                throw new IllegalArgumentException("La URL pública debe empezar con https:// (Shopify solo envía webhooks por HTTPS)");
            }
            if (!url.equals(webhookSettings.getPublicUrl())) {
                webhookSettings.setPublicUrl(url);
                changes.add("URL pública: " + (url.isEmpty() ? "(vacía)" : url));
            }
        }
        if (Boolean.TRUE.equals(body.get("clearSecret"))) {
            webhookSettings.setSecret("");
            changes.add("llave: se usa la del instalador");
        } else if (body.get("secret") != null && !String.valueOf(body.get("secret")).isBlank()) {
            webhookSettings.setSecret(String.valueOf(body.get("secret")));
            changes.add("llave cambiada (" + webhookSettings.getSecretHint() + ")");
        }
        if (body.get("modes") instanceof Map<?, ?> modes) {
            for (Map.Entry<?, ?> entry : modes.entrySet()) {
                WebhookSettingsService.Endpoint endpoint = webhookSettings.findByKey(String.valueOf(entry.getKey()));
                String mode = String.valueOf(entry.getValue());
                if (!mode.equals(webhookSettings.getMode(endpoint))) {
                    webhookSettings.setMode(endpoint, mode);
                    changes.add(endpoint.label() + ": " + mode);
                }
            }
        }
        if (!changes.isEmpty()) {
            audit.log(AdminControllerSupport.user(request), "CONFIG_WEBHOOKS", String.join(" | ", changes));
        }
        return config();
    }

    /** Versión de la API de Shopify que usa el servicio (de shopify.url: .../admin/api/2026-04/...). */
    private String apiVersion() {
        Matcher m = Pattern.compile("/admin/api/([^/]+)/").matcher(shopifyUrl == null ? "" : shopifyUrl);
        return m.find() ? m.group(1) : "";
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
