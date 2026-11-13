package com.retailmanager.rmpayCalendar.services.services.Shopify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.retailmanager.rmpayCalendar.db2.repository.FailedOrderRepository;
import com.retailmanager.rmpayCalendar.db2.repository.ProcessedOrderRepository;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;
import com.retailmanager.rmpayCalendar.services.services.webhook.WebhookSettingsService;

/**
 * Facturas del webhook de pago: prefijo KW (el ciclo usa K) en el encabezado y en todas las
 * líneas, y reserva del pedido en ProcessedOrder antes de enviarlo a RM para no facturarlo dos veces.
 */
class WebhookInvoicePrefixTest {

    private static final String SECRET = "llave-de-prueba";
    private static final String PAYLOAD = "{\"admin_graphql_api_id\":\"gid://shopify/Order/77\",\"name\":\"#1077\","
            + "\"financial_status\":\"paid\",\"created_at\":\"2026-11-09T10:00:00-04:00\",\"subtotal_price\":\"45.00\","
            + "\"total_price\":\"63.18\",\"total_tax\":\"5.18\",\"taxes_included\":false,"
            + "\"tax_lines\":[{\"title\":\"PR State Tax\",\"rate\":0.105,\"price\":\"4.73\"},{\"title\":\"PR Municipal Tax\",\"rate\":0.01,\"price\":\"0.45\"}],"
            + "\"line_items\":[{\"price\":\"50.00\",\"quantity\":1,\"sku\":\"SKU1\",\"name\":\"P\"}]}";

    private InvoiceSyncService service;
    private PosClientService pos;
    private ProcessedOrderRepository processed;

    @BeforeEach
    void setUp() {
        service = new InvoiceSyncService();
        pos = mock(PosClientService.class);
        processed = mock(ProcessedOrderRepository.class);
        WebhookSettingsService settings = mock(WebhookSettingsService.class);
        when(settings.getSecret(any())).thenReturn(SECRET);
        ReflectionTestUtils.setField(service, "posClientService", pos);
        ReflectionTestUtils.setField(service, "repository", processed);
        ReflectionTestUtils.setField(service, "failedOrderRepository", mock(FailedOrderRepository.class));
        ReflectionTestUtils.setField(service, "webhookSettingsService", settings);
        ReflectionTestUtils.setField(service, "webhookSecret", SECRET);
        ReflectionTestUtils.setField(service, "shippingItemCode", "FLETE");
        ReflectionTestUtils.setField(service, "tipItemCode", "PROPINA");
    }

    private static String hmac(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void facturaDelWebhookUsaPrefijoKWEnTodasLasLineas() throws Exception {
        ResponseEntity<?> response = service.receiveOrder(hmac(PAYLOAD), PAYLOAD);

        assertEquals(200, response.getStatusCode().value());
        ArgumentCaptor<Map<String, Object>> sent = ArgumentCaptor.forClass(Map.class);
        verify(pos).sendInvoiceToPOS(sent.capture());
        Map<String, Object> invoice = sent.getValue();
        assertEquals("KW1077", invoice.get("Factura"));
        List<Map<String, Object>> items = (List<Map<String, Object>>) invoice.get("Items");
        assertEquals(2, items.size(), "producto + línea FLETE de envío");
        items.forEach(line -> assertEquals("KW1077", line.get("FacturaNumero")));
        verify(processed).reserve(eq("gid://shopify/Order/77"), any(), eq(PAYLOAD), anyString(), any());
        verify(processed).setRmInvoice("gid://shopify/Order/77", "KW1077");
    }

    @Test
    void siOtroCaminoYaLoReservoNoSeEnviaOtraFactura() throws Exception {
        when(processed.reserve(anyString(), any(), anyString(), anyString(), any()))
                .thenThrow(new DataIntegrityViolationException("PK duplicada"));

        ResponseEntity<?> response = service.receiveOrder(hmac(PAYLOAD), PAYLOAD);

        assertEquals(200, response.getStatusCode().value());
        verify(pos, never()).sendInvoiceToPOS(any());
    }

    @Test
    void siRmFallaSeLiberaLaReservaYShopifyReintenta() throws Exception {
        doThrow(new RuntimeException("POS rechazó la factura")).when(pos).sendInvoiceToPOS(any());

        ResponseEntity<?> response = service.receiveOrder(hmac(PAYLOAD), PAYLOAD);

        assertEquals(500, response.getStatusCode().value());
        verify(processed).deleteById("gid://shopify/Order/77");
    }
}
