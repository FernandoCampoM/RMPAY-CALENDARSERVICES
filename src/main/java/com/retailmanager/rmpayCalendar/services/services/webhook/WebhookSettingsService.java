package com.retailmanager.rmpayCalendar.services.services.webhook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpayCalendar.admin.service.SyncSettingsService;

/**
 * Configuración de los webhooks de Shopify editable desde el panel (guardada en Sys_general_config):
 * URL pública del servicio, llave de firma y modo de cada webhook (ACTIVO o SOLO_REGISTRO).
 * La llave del panel tiene prioridad; si está vacía se usa shopify.webhook_secret (la del instalador).
 * Depende de SyncSettingsService (lectura/escritura en Sys_general_config).
 */
@Service
public class WebhookSettingsService {

    public static final String PUBLIC_URL = "webhook.publicUrl";
    public static final String SECRET = "webhook.secret";
    public static final String MODE_ACTIVE = "ACTIVO";
    public static final String MODE_LOG_ONLY = "SOLO_REGISTRO";

    /** Webhooks que atiende el servicio: clave, ruta, evento de Shopify y nombre en el admin de Shopify. */
    public record Endpoint(String key, String path, String topic, String label) {
        String modeLabel() {
            return "webhook." + key + ".mode";
        }
    }

    public static final List<Endpoint> ENDPOINTS = List.of(
            new Endpoint("orders", "/webhook/orders", "orders/paid", "Pago de pedido"),
            new Endpoint("cancelled", "/webhook/orders/cancelled", "orders/cancelled", "Cancelación de pedido"),
            new Endpoint("refunds", "/webhook/refunds", "refunds/create", "Creación de reembolso"));

    @Autowired
    private SyncSettingsService settings;

    @Value("${shopify.webhook_secret:}")
    private String installerSecret;

    /** Llave del panel o, si no hay, la indicada (la de application.properties). */
    public String getSecret(String fallback) {
        String panel = settings.get(SECRET, "");
        return panel.isBlank() ? fallback : panel;
    }

    public String getSecret() {
        return getSecret(installerSecret);
    }

    /** PANEL, INSTALADOR o NINGUNA */
    public String getSecretSource() {
        if (!settings.get(SECRET, "").isBlank()) {
            return "PANEL";
        }
        return installerSecret == null || installerSecret.isBlank() ? "NINGUNA" : "INSTALADOR";
    }

    /** Últimos 4 caracteres de la llave en uso, para reconocerla sin mostrarla. */
    public String getSecretHint() {
        String secret = getSecret();
        if (secret == null || secret.isBlank()) {
            return "";
        }
        return "••••" + secret.substring(Math.max(0, secret.length() - 4));
    }

    public void setSecret(String secret) {
        settings.set(SECRET, "WEBHOOK SECRET", secret == null ? "" : secret.trim());
    }

    public String getPublicUrl() {
        return settings.get(PUBLIC_URL, "");
    }

    public void setPublicUrl(String url) {
        String value = url == null ? "" : url.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        settings.set(PUBLIC_URL, "WEBHOOK PUBLIC URL", value);
    }

    public String getMode(Endpoint endpoint) {
        String mode = settings.get(endpoint.modeLabel(), MODE_ACTIVE);
        return MODE_LOG_ONLY.equals(mode) ? MODE_LOG_ONLY : MODE_ACTIVE;
    }

    public void setMode(Endpoint endpoint, String mode) {
        if (!MODE_ACTIVE.equals(mode) && !MODE_LOG_ONLY.equals(mode)) {
            throw new IllegalArgumentException("Modo de webhook no válido: " + mode);
        }
        settings.set(endpoint.modeLabel(), "WEBHOOK MODE " + endpoint.key().toUpperCase(), mode);
    }

    public Endpoint findByKey(String key) {
        return ENDPOINTS.stream().filter(e -> e.key().equals(key)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Webhook desconocido: " + key));
    }

    /** false solo si la ruta es un webhook conocido en modo SOLO_REGISTRO. */
    public boolean isProcessing(String path) {
        for (Endpoint endpoint : ENDPOINTS) {
            if (endpoint.path().equals(path)) {
                return MODE_ACTIVE.equals(getMode(endpoint));
            }
        }
        return true;
    }

    /** Firma HMAC-SHA256 en Base64 del body, como la calcula Shopify. */
    public boolean isValidSignature(byte[] body, String hmacHeader) {
        String secret = getSecret();
        if (secret == null || secret.isBlank() || hmacHeader == null) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = Base64.getEncoder().encode(mac.doFinal(body));
            return MessageDigest.isEqual(expected, hmacHeader.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}
