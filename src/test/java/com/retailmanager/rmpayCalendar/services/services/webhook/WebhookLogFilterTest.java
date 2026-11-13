package com.retailmanager.rmpayCalendar.services.services.webhook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpayCalendar.db2.entity.WebhookLog;
import com.retailmanager.rmpayCalendar.db2.repository.ProcessedOrderRepository;
import com.retailmanager.rmpayCalendar.services.services.Shopify.InvoiceSyncService;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

import jakarta.servlet.FilterChain;

/**
 * Traza de webhooks: el filtro WebhookLogFilter alrededor de InvoiceSyncService.receiveOrder,
 * con las anotaciones de WebhookTrace. Sin Spring, base de datos ni red.
 */
class WebhookLogFilterTest {

    private static final String SECRET = "secreto-de-prueba";
    private static final String ORDER = "{\"admin_graphql_api_id\":\"gid://shopify/Order/77\",\"name\":\"#1077\","
            + "\"financial_status\":\"paid\",\"created_at\":\"2026-11-09T10:00:00-04:00\",\"subtotal_price\":\"45.00\","
            + "\"total_price\":\"58.17\",\"total_tax\":\"5.18\",\"taxes_included\":false,"
            + "\"tax_lines\":[{\"title\":\"PR State Tax\",\"rate\":0.105,\"price\":\"4.73\"},{\"title\":\"PR Municipal Tax\",\"rate\":0.01,\"price\":\"0.45\"}],"
            + "\"line_items\":[{\"price\":\"50.00\",\"quantity\":1,\"sku\":\"SKU1\",\"name\":\"P\"}]}";

    private OncePerRequestFilter filter;
    private WebhookLogService logService;
    private InvoiceSyncService invoiceSyncService;
    private PosClientService pos;

    private WebhookSettingsService webhookSettings;

    @BeforeEach
    void setUp() {
        webhookSettings = WebhookSettingsServiceTest.settings(SECRET);
        logService = mock(WebhookLogService.class);
        WebhookLogFilter config = new WebhookLogFilter();
        ReflectionTestUtils.setField(config, "webhookLogService", logService);
        ReflectionTestUtils.setField(config, "webhookSettingsService", webhookSettings);
        filter = config.webhookTraceFilter().getFilter();

        invoiceSyncService = new InvoiceSyncService();
        pos = mock(PosClientService.class);
        ReflectionTestUtils.setField(invoiceSyncService, "posClientService", pos);
        ReflectionTestUtils.setField(invoiceSyncService, "repository", mock(ProcessedOrderRepository.class));
        ReflectionTestUtils.setField(invoiceSyncService, "webhookSecret", SECRET);
        ReflectionTestUtils.setField(invoiceSyncService, "webhookSettingsService", webhookSettings);
        ReflectionTestUtils.setField(invoiceSyncService, "shippingItemCode", "FLETE");
        ReflectionTestUtils.setField(invoiceSyncService, "tipItemCode", "PROPINA");
    }

    private static String sign(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(body.getBytes()));
    }

    /** Como el controlador: lee el body, llama a receiveOrder y escribe la respuesta. */
    private FilterChain controller() {
        return (req, res) -> {
            String hmac = ((jakarta.servlet.http.HttpServletRequest) req).getHeader("X-Shopify-Hmac-Sha256");
            String body = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            ResponseEntity<?> entity = invoiceSyncService.receiveOrder(hmac, body);
            ((jakarta.servlet.http.HttpServletResponse) res).setStatus(entity.getStatusCode().value());
            if (entity.getBody() != null) {
                res.getOutputStream().write(new ObjectMapper().writeValueAsBytes(entity.getBody()));
            }
        };
    }

    private WebhookLog run(String hmac, FilterChain chain, MockHttpServletResponse response) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/webhook/orders");
        request.setRequestURI("/webhook/orders");
        request.setContent(ORDER.getBytes(StandardCharsets.UTF_8));
        request.addHeader("X-Shopify-Topic", "orders/paid");
        request.addHeader("X-Shopify-Webhook-Id", "wh-123");
        if (hmac != null) {
            request.addHeader("X-Shopify-Hmac-Sha256", hmac);
        }
        filter.doFilter(request, response, chain);
        ArgumentCaptor<WebhookLog> saved = ArgumentCaptor.forClass(WebhookLog.class);
        verify(logService).save(saved.capture());
        return saved.getValue();
    }

    @Test
    void pedidoProcesadoGuardaBodyFacturaYResultado() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        WebhookLog log = run(sign(ORDER), controller(), response);

        assertEquals("PROCESADA", log.getResult());
        assertEquals(200, log.getHttpStatus());
        assertEquals("gid://shopify/Order/77", log.getShopifyOrderId());
        assertEquals("#1077", log.getOrderName());
        assertEquals("orders/paid", log.getTopic());
        assertEquals("wh-123", log.getWebhookId());
        assertTrue(log.getHmacValid());
        assertEquals(ORDER, log.getPayload());
        assertTrue(log.getInvoiceJson().contains("FLETE"), "la factura enviada queda en la traza");
        assertTrue(log.getDetail().contains("KW1077"), "las facturas del webhook llevan el prefijo KW");
        assertNull(log.getError());
        assertNotNull(log.getDurationMs());
        // la respuesta llega igual a Shopify
        assertTrue(response.getContentAsString().contains("\"success\":true"));
    }

    @Test
    void errorDeRmGuardaMensajeYTraza() throws Exception {
        doThrow(new RuntimeException("POS rechazó la factura | Product not found: SKU1")).when(pos).sendInvoiceToPOS(any());

        WebhookLog log = run(sign(ORDER), controller(), new MockHttpServletResponse());

        assertEquals("ERROR", log.getResult());
        assertEquals(500, log.getHttpStatus());
        assertTrue(log.getError().contains("Product not found"));
        assertTrue(log.getStackTrace().contains("RuntimeException"));
        assertNotNull(log.getInvoiceJson(), "también queda la factura que se intentó enviar");
    }

    @Test
    void firmaInvalida() throws Exception {
        WebhookLog log = run("firmaFalsa==", controller(), new MockHttpServletResponse());

        assertEquals("FIRMA_INVALIDA", log.getResult());
        assertEquals(401, log.getHttpStatus());
        assertFalse(log.getHmacValid());
        assertEquals(ORDER, log.getPayload());
    }

    @Test
    void peticionRechazadaAntesDelControladorTambienQueda() throws Exception {
        // Como cuando Spring responde 400 por falta del header de firma, sin leer el body
        FilterChain rejected = (req, res) -> ((jakarta.servlet.http.HttpServletResponse) res).setStatus(400);

        WebhookLog log = run(null, rejected, new MockHttpServletResponse());

        assertEquals("SIN_FIRMA", log.getResult());
        assertEquals(400, log.getHttpStatus());
        assertFalse(log.getHmacPresent());
        assertEquals(ORDER, log.getPayload());
    }

    @Test
    void soloRegistrarCompruebaLaFirmaYNoProcesa() throws Exception {
        webhookSettings.setMode(webhookSettings.findByKey("orders"), WebhookSettingsService.MODE_LOG_ONLY);
        MockHttpServletResponse response = new MockHttpServletResponse();

        WebhookLog log = run(sign(ORDER), controller(), response);

        assertEquals("SOLO_REGISTRO", log.getResult());
        assertEquals(200, log.getHttpStatus());
        assertTrue(log.getHmacValid());
        assertEquals(ORDER, log.getPayload());
        assertNull(log.getInvoiceJson());
        verify(pos, org.mockito.Mockito.never()).sendInvoiceToPOS(any());
        assertTrue(response.getContentAsString().contains("\"processed\":false"));
    }

    @Test
    void soloRegistrarDetectaFirmaInvalida() throws Exception {
        webhookSettings.setMode(webhookSettings.findByKey("orders"), WebhookSettingsService.MODE_LOG_ONLY);

        WebhookLog log = run("firmaFalsa==", controller(), new MockHttpServletResponse());

        assertEquals("SOLO_REGISTRO", log.getResult());
        assertFalse(log.getHmacValid());
    }

    @Test
    void laLlaveDelPanelReemplazaLaDelInstalador() throws Exception {
        webhookSettings.setSecret("llave-del-panel");

        // firmado con la llave vieja (instalador): ahora es inválido
        WebhookLog log = run(sign(ORDER), controller(), new MockHttpServletResponse());

        assertEquals("FIRMA_INVALIDA", log.getResult());
        assertEquals(401, log.getHttpStatus());
    }

    @Test
    void fueraDeUnWebhookLaTrazaNoHaceNada() {
        WebhookTrace.result("X", "y");
        WebhookTrace.error(new RuntimeException("z"));
        WebhookTrace.invoice(java.util.Map.of("a", 1));
        assertNull(WebhookTrace.end());
    }
}
