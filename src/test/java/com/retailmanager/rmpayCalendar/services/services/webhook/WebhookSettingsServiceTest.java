package com.retailmanager.rmpayCalendar.services.services.webhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.retailmanager.rmpayCalendar.admin.service.SyncSettingsService;

/**
 * Configuración de webhooks del panel (WebhookSettingsService): llave, modos y firma.
 * Sys_general_config se simula con un mapa en memoria.
 */
class WebhookSettingsServiceTest {

    /** WebhookSettingsService con la llave del instalador indicada y la configuración en memoria. */
    static WebhookSettingsService settings(String installerSecret) {
        Map<String, String> store = new HashMap<>();
        SyncSettingsService sync = mock(SyncSettingsService.class);
        when(sync.get(anyString(), anyString())).thenAnswer(i -> store.getOrDefault(i.getArgument(0), i.getArgument(1)));
        doAnswer(i -> store.put(i.getArgument(0), i.getArgument(2))).when(sync).set(anyString(), anyString(), anyString());
        WebhookSettingsService service = new WebhookSettingsService();
        ReflectionTestUtils.setField(service, "settings", sync);
        ReflectionTestUtils.setField(service, "installerSecret", installerSecret);
        return service;
    }

    private static String sign(String secret, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void llaveDelInstaladorPanelYVuelta() {
        WebhookSettingsService s = settings("instalador-1234");
        assertEquals("INSTALADOR", s.getSecretSource());
        assertEquals("••••1234", s.getSecretHint());

        s.setSecret("  panel-abcd ");
        assertEquals("PANEL", s.getSecretSource());
        assertEquals("panel-abcd", s.getSecret());
        assertEquals("panel-abcd", s.getSecret("otra"));

        s.setSecret("");
        assertEquals("instalador-1234", s.getSecret());
        assertEquals("NINGUNA", settings("").getSecretSource());
    }

    @Test
    void modosPorWebhook() {
        WebhookSettingsService s = settings("x");
        assertTrue(s.isProcessing("/webhook/orders"));

        s.setMode(s.findByKey("refunds"), WebhookSettingsService.MODE_LOG_ONLY);
        assertFalse(s.isProcessing("/webhook/refunds"));
        assertTrue(s.isProcessing("/webhook/orders"));
        assertTrue(s.isProcessing("/webhook/otra-ruta"));
        assertThrows(IllegalArgumentException.class, () -> s.setMode(s.findByKey("orders"), "APAGADO"));
        assertThrows(IllegalArgumentException.class, () -> s.findByKey("nada"));
    }

    @Test
    void urlPublicaSinBarraFinal() {
        WebhookSettingsService s = settings("x");
        s.setPublicUrl(" https://rmpay.mitienda.com/// ");
        assertEquals("https://rmpay.mitienda.com", s.getPublicUrl());
    }

    @Test
    void firmaComoLaCalculaShopify() throws Exception {
        WebhookSettingsService s = settings("secreto");
        String body = "{\"id\":1,\"name\":\"#1001 ñ\"}";
        assertTrue(s.isValidSignature(body.getBytes(StandardCharsets.UTF_8), sign("secreto", body)));
        assertFalse(s.isValidSignature(body.getBytes(StandardCharsets.UTF_8), sign("otro", body)));
        assertFalse(s.isValidSignature(body.getBytes(StandardCharsets.UTF_8), null));
        assertFalse(settings("").isValidSignature(body.getBytes(StandardCharsets.UTF_8), sign("secreto", body)));
    }
}
