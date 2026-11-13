package com.retailmanager.rmpayCalendar.services.services.Shopify;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.retailmanager.rmpayCalendar.db2.entity.FailedOrder;
import com.retailmanager.rmpayCalendar.db2.repository.FailedOrderRepository;
import com.retailmanager.rmpayCalendar.db2.repository.ProcessedOrderRepository;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

/**
 * El ciclo reserva el pedido por su ID de Shopify con INSERT (como el webhook), así un pedido no se
 * factura dos veces aunque el ciclo use K y el webhook KW; y cada camino guarda el número de
 * factura con que quedó en RM para mostrarlo en el panel (Pedidos).
 */
class RmInvoiceNumberTest {

    private static final String ID = "gid://shopify/Order/1006";
    private static final String ORDER = "{\"id\":\"" + ID + "\",\"name\":\"#1006\",\"createdAt\":\"2026-11-12T10:00:00Z\","
            + "\"displayFinancialStatus\":\"PAID\",\"taxesIncluded\":false,"
            + "\"subtotalPriceSet\":{\"shopMoney\":{\"amount\":\"50.00\"}},\"totalPriceSet\":{\"shopMoney\":{\"amount\":\"50.00\"}},"
            + "\"totalTaxSet\":{\"shopMoney\":{\"amount\":\"0.00\"}},\"taxLines\":[],"
            + "\"totalTipReceivedSet\":{\"shopMoney\":{\"amount\":\"0.00\"}},"
            + "\"lineItems\":{\"edges\":[{\"node\":{\"name\":\"P\",\"quantity\":1,"
            + "\"originalUnitPriceSet\":{\"shopMoney\":{\"amount\":\"50.00\"}},\"variant\":{\"sku\":\"S\"}}}]}}";
    private static final String PAGE = "{\"data\":{\"orders\":{\"edges\":[{\"node\":" + ORDER + "}],\"pageInfo\":{\"hasNextPage\":false}}}}";

    private InvoiceSyncService service;
    private PosClientService pos;
    private ProcessedOrderRepository processed;
    private FailedOrderRepository failed;

    @BeforeEach
    void setUp() {
        service = new InvoiceSyncService();
        IShopifyService shopify = mock(IShopifyService.class);
        when(shopify.getNewOrders(anyString(), any())).thenReturn(PAGE);
        pos = mock(PosClientService.class);
        processed = mock(ProcessedOrderRepository.class);
        failed = mock(FailedOrderRepository.class);
        when(failed.findById(anyString())).thenReturn(Optional.empty());
        ReflectionTestUtils.setField(service, "shopifyService", shopify);
        ReflectionTestUtils.setField(service, "posClientService", pos);
        ReflectionTestUtils.setField(service, "repository", processed);
        ReflectionTestUtils.setField(service, "failedOrderRepository", failed);
        ReflectionTestUtils.setField(service, "shippingItemCode", "FLETE");
        ReflectionTestUtils.setField(service, "tipItemCode", "PROPINA");
    }

    @Test
    void elCicloReservaConInsertYGuardaLaFacturaK() throws Exception {
        service.syncOrdersToPOS("2026-11-12T05:00:00Z");

        verify(processed).reserve(eq(ID), any(), anyString(), eq("PAID"), any());
        verify(processed, never()).save(any());
        verify(pos).sendInvoiceToPOS(any());
        verify(processed).setRmInvoice(ID, "K1006");
    }

    @Test
    void siElWebhookYaLoReservoElCicloNoFactura() throws Exception {
        when(processed.reserve(anyString(), any(), anyString(), anyString(), any()))
                .thenThrow(new DataIntegrityViolationException("PK duplicada"));

        service.syncOrdersToPOS("2026-11-12T05:00:00Z");

        verify(pos, never()).sendInvoiceToPOS(any());
        verify(processed, never()).setRmInvoice(anyString(), anyString());
    }

    @Test
    void facturaQueYaExistiaEnRmGuardaSuNumero() throws Exception {
        doThrow(new RuntimeException("Invoice already exists")).when(pos).sendInvoiceToPOS(any());

        service.syncOrdersToPOS("2026-11-12T05:00:00Z");

        verify(processed).setRmInvoice(ID, "K1006");
        verify(processed, never()).deleteById(ID);
    }

    @Test
    void reintentoExitosoGuardaLaFacturaK() {
        FailedOrder f = new FailedOrder();
        f.setId(ID);
        f.setAttempts(1);
        f.setOrderJson(ORDER);

        assertTrue(service.retryFailedOrder(f));

        verify(processed).setRmInvoice(ID, "K1006");
    }
}
