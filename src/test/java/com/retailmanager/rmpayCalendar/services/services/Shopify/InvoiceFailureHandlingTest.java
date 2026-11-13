package com.retailmanager.rmpayCalendar.services.services.Shopify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpayCalendar.db2.entity.FailedOrder;
import com.retailmanager.rmpayCalendar.db2.entity.ProcessedOrder;
import com.retailmanager.rmpayCalendar.db2.repository.FailedOrderRepository;
import com.retailmanager.rmpayCalendar.db2.repository.ProcessedOrderRepository;
import com.retailmanager.rmpayCalendar.db2.repository.Sys_general_configRepository;
import com.retailmanager.rmpayCalendar.services.services.pos.ChargeItemsService;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

/**
 * Propina, montos del webhook, simulación, facturas fallidas y reintentos de InvoiceSyncService
 * e InvoiceSyncJob. Sin Spring, sin base de datos y sin red (todo con mocks).
 */
class InvoiceFailureHandlingTest {

    private InvoiceSyncService service;
    private IShopifyService shopify;
    private PosClientService pos;
    private ProcessedOrderRepository processed;
    private FailedOrderRepository failed;

    @BeforeEach
    void setUp() {
        service = new InvoiceSyncService();
        shopify = mock(IShopifyService.class);
        pos = mock(PosClientService.class);
        processed = mock(ProcessedOrderRepository.class);
        failed = mock(FailedOrderRepository.class);
        ReflectionTestUtils.setField(service, "shopifyService", shopify);
        ReflectionTestUtils.setField(service, "posClientService", pos);
        ReflectionTestUtils.setField(service, "repository", processed);
        ReflectionTestUtils.setField(service, "failedOrderRepository", failed);
        ReflectionTestUtils.setField(service, "shippingItemCode", "FLETE");
        ReflectionTestUtils.setField(service, "tipItemCode", "PROPINA");
        when(failed.findById(anyString())).thenReturn(Optional.empty());
    }

    /** Pedido PAID de la consulta GraphQL: bruto 50, subtotal y total a elección, sin impuestos. */
    private static String order(String id, double subtotal, double total, double tip) {
        return String.format(Locale.ROOT,
                "{\"id\":\"%s\",\"name\":\"#1001\",\"createdAt\":\"2026-11-03T10:00:00Z\",\"displayFinancialStatus\":\"PAID\","
                        + "\"taxesIncluded\":false,\"subtotalPriceSet\":{\"shopMoney\":{\"amount\":\"%.2f\"}},"
                        + "\"totalPriceSet\":{\"shopMoney\":{\"amount\":\"%.2f\"}},"
                        + "\"totalTaxSet\":{\"shopMoney\":{\"amount\":\"0.00\"}},\"taxLines\":[],"
                        + "\"totalTipReceivedSet\":{\"shopMoney\":{\"amount\":\"%.2f\"}},"
                        + "\"lineItems\":{\"edges\":[{\"node\":{\"name\":\"P\",\"quantity\":1,"
                        + "\"originalUnitPriceSet\":{\"shopMoney\":{\"amount\":\"50.00\"}},\"variant\":{\"sku\":\"S\"}}}]}}",
                id, subtotal, total, tip);
    }

    private static String page(String order) {
        return "{\"data\":{\"orders\":{\"edges\":[{\"node\":" + order + "}],\"pageInfo\":{\"hasNextPage\":false}}}}";
    }

    @Test
    @SuppressWarnings("unchecked")
    void propinaVaEnSuPropiaLinea() throws Exception {
        // subtotal 50 + envío 5 + propina 3 = 58
        Map<String, Object> invoice = ReflectionTestUtils.invokeMethod(service, "mapOrderFromQueryToInvoice",
                new ObjectMapper().readTree(order("gid://shopify/Order/1", 50, 58, 3)));

        List<Map<String, Object>> items = (List<Map<String, Object>>) invoice.get("Items");
        assertEquals(3.0, (double) items.stream().filter(i -> "PROPINA".equals(i.get("Referencia"))).findFirst().get().get("Precio"), 0.001);
        assertEquals(5.0, (double) items.stream().filter(i -> "FLETE".equals(i.get("Referencia"))).findFirst().get().get("Precio"), 0.001);
        assertEquals(58.0, (double) invoice.get("Subtotal"), 0.001);
        assertEquals(58.0, (double) invoice.get("Total"), 0.001);
    }

    @Test
    @SuppressWarnings("unchecked")
    void webhookCumpleLaReglaDeMontos() throws Exception {
        // REST: price = precio original; subtotal_price ya con descuento; total con IVU y envío 7.99
        String payload = "{\"name\":\"#1002\",\"created_at\":\"2026-11-03T10:00:00-04:00\","
                + "\"subtotal_price\":\"45.00\",\"total_price\":\"58.17\",\"total_tax\":\"5.18\",\"taxes_included\":false,"
                + "\"total_tip_received\":\"0.00\","
                + "\"tax_lines\":[{\"title\":\"PR State Tax\",\"rate\":0.105,\"price\":\"4.73\"},"
                + "{\"title\":\"PR Municipal Tax\",\"rate\":0.01,\"price\":\"0.45\"}],"
                + "\"line_items\":[{\"price\":\"25.00\",\"quantity\":2,\"sku\":\"S\",\"name\":\"P\"}]}";

        Map<String, Object> invoice = ReflectionTestUtils.invokeMethod(service, "mapOrderToInvoice",
                new ObjectMapper().readTree(payload));

        double s = (double) invoice.get("Subtotal"), d = (double) invoice.get("Descuento");
        double ct = (double) invoice.get("CityTax"), st = (double) invoice.get("StateTax");
        assertEquals(58.17, Math.round((s - d + ct + st) * 100) / 100.0, 0.001);
        assertEquals(5.00, d, 0.001);
        assertEquals(0.45, ct, 0.001);
        assertEquals(4.73, st, 0.001);
        assertEquals(58.17, (double) invoice.get("PaidCash"), 0.001);
        List<Map<String, Object>> items = (List<Map<String, Object>>) invoice.get("Items");
        assertTrue(items.stream().anyMatch(i -> "FLETE".equals(i.get("Referencia"))));
    }

    @Test
    void simulacionNoEnviaNiRegistraFallidas() throws Exception {
        ReflectionTestUtils.setField(service, "invoiceDryRun", true);
        when(shopify.getNewOrders(any(), any())).thenReturn(page(order("gid://shopify/Order/1", 50, 50, 0)));

        service.syncOrdersToPOS("2026-11-01T00:00:00Z");

        verify(pos, never()).sendInvoiceToPOS(any());
        verify(processed).deleteById("gid://shopify/Order/1");
        verify(failed, never()).save(any());
    }

    @Test
    void montosQueNoCuadranNoSeEnvianYQuedanFallidos() throws Exception {
        // total menor que el subtotal: no hay forma de cuadrarlo
        when(shopify.getNewOrders(any(), any())).thenReturn(page(order("gid://shopify/Order/2", 45, 40, 0)));

        service.syncOrdersToPOS("2026-11-01T00:00:00Z");

        verify(pos, never()).sendInvoiceToPOS(any());
        verify(processed).deleteById("gid://shopify/Order/2");
        ArgumentCaptor<FailedOrder> saved = ArgumentCaptor.forClass(FailedOrder.class);
        verify(failed).save(saved.capture());
        assertEquals(1, saved.getValue().getAttempts());
        assertTrue(saved.getValue().getLastError().contains("Montos no cuadran"));
    }

    @Test
    void rechazoDeRmQuedaFallido() throws Exception {
        when(shopify.getNewOrders(any(), any())).thenReturn(page(order("gid://shopify/Order/3", 50, 50, 0)));
        doThrow(new RuntimeException("POS rechazó la factura | ItemCode not found")).when(pos).sendInvoiceToPOS(any());

        service.syncOrdersToPOS("2026-11-01T00:00:00Z");

        verify(processed).deleteById("gid://shopify/Order/3");
        ArgumentCaptor<FailedOrder> saved = ArgumentCaptor.forClass(FailedOrder.class);
        verify(failed).save(saved.capture());
        assertEquals("gid://shopify/Order/3", saved.getValue().getId());
        assertTrue(saved.getValue().getOrderJson().contains("\"name\":\"#1001\""));
    }

    @Test
    void facturaQueYaExisteNoQuedaFallida() throws Exception {
        when(shopify.getNewOrders(any(), any())).thenReturn(page(order("gid://shopify/Order/4", 50, 50, 0)));
        doThrow(new RuntimeException("Invoice already exists")).when(pos).sendInvoiceToPOS(any());

        service.syncOrdersToPOS("2026-11-01T00:00:00Z");

        verify(processed, never()).deleteById(any());
        verify(failed, never()).save(any());
    }

    @Test
    void reintentoExitosoMarcaProcesadaYBorraLaFallida() {
        FailedOrder f = new FailedOrder();
        f.setId("gid://shopify/Order/5");
        f.setAttempts(2);
        f.setOrderJson(order("gid://shopify/Order/5", 50, 50, 0));

        assertTrue(service.retryFailedOrder(f));

        verify(pos).sendInvoiceToPOS(any());
        ArgumentCaptor<ProcessedOrder> saved = ArgumentCaptor.forClass(ProcessedOrder.class);
        verify(processed).save(saved.capture());
        assertEquals("PAID", saved.getValue().getEventType());
        verify(failed).delete(f);
    }

    @Test
    void reintentoFallidoSumaUnIntento() {
        FailedOrder f = new FailedOrder();
        f.setId("gid://shopify/Order/6");
        f.setAttempts(2);
        f.setOrderJson(order("gid://shopify/Order/6", 50, 50, 0));
        when(failed.findById("gid://shopify/Order/6")).thenReturn(Optional.of(f));
        doThrow(new RuntimeException("RM caído")).when(pos).sendInvoiceToPOS(any());

        assertFalse(service.retryFailedOrder(f));

        assertEquals(3, f.getAttempts());
        assertEquals("RM caído", f.getLastError());
        verify(failed, never()).delete(any());
        verify(processed, never()).save(any());
    }

    @Test
    void reintentosSoloTomanLosQueNoLleganAlMaximo() {
        when(failed.findByAttemptsLessThanOrderByFirstFailedAtAsc(InvoiceSyncService.MAX_FAILED_ATTEMPTS)).thenReturn(List.of());

        service.retryFailedOrders();

        verify(failed).findByAttemptsLessThanOrderByFirstFailedAtAsc(eq(InvoiceSyncService.MAX_FAILED_ATTEMPTS));
        verify(pos, never()).sendInvoiceToPOS(any());
    }

    @Test
    void jobEnSimulacionNoAvanzaLaFecha() throws Exception {
        ReflectionTestUtils.setField(service, "invoiceDryRun", true);
        when(shopify.getNewOrders(any(), any())).thenReturn(page(order("gid://shopify/Order/7", 50, 50, 0)));
        Sys_general_configRepository config = mock(Sys_general_configRepository.class);
        when(config.getLastSyncDate()).thenReturn("2026-11-01T00:00:00Z");

        InvoiceSyncJob job = new InvoiceSyncJob();
        ReflectionTestUtils.setField(job, "invoiceSyncService", service);
        ReflectionTestUtils.setField(job, "configRepository", config);
        ReflectionTestUtils.setField(job, "chargeItemsService", mock(ChargeItemsService.class));
        job.execute();

        verify(config, never()).updateLastSyncDate(any());
    }

    @Test
    void jobNormalAvanzaLaFecha() throws Exception {
        when(shopify.getNewOrders(any(), any())).thenReturn("{\"data\":{\"orders\":{\"edges\":[],\"pageInfo\":{\"hasNextPage\":false}}}}");
        when(failed.findByAttemptsLessThanOrderByFirstFailedAtAsc(InvoiceSyncService.MAX_FAILED_ATTEMPTS)).thenReturn(List.of());
        Sys_general_configRepository config = mock(Sys_general_configRepository.class);
        when(config.getLastSyncDate()).thenReturn("2026-11-01T00:00:00Z");

        InvoiceSyncJob job = new InvoiceSyncJob();
        ReflectionTestUtils.setField(job, "invoiceSyncService", service);
        ReflectionTestUtils.setField(job, "configRepository", config);
        ReflectionTestUtils.setField(job, "chargeItemsService", mock(ChargeItemsService.class));
        job.execute();

        verify(config).updateLastSyncDate(any());
    }
}
