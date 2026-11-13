package com.retailmanager.rmpayCalendar.services.services.Shopify;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.retailmanager.rmpayCalendar.db2.repository.Sys_general_configRepository;
import com.retailmanager.rmpayCalendar.services.services.pos.ChargeItemsService;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

/**
 * Si Shopify niega la consulta de pedidos (por ejemplo "not approved to access the Order object"),
 * el ciclo no debe tomarlo como "0 pedidos" ni avanzar lastSyncDate: esos pedidos se pierden.
 */
class OrderQueryErrorTest {

    private static final String DENIED = "{\"errors\":[{\"message\":\"This app is not approved to access the Order object.\","
            + "\"extensions\":{\"code\":\"ACCESS_DENIED\"},\"path\":[\"orders\"]}],\"data\":null}";
    private static final String EMPTY = "{\"data\":{\"orders\":{\"edges\":[],\"pageInfo\":{\"hasNextPage\":false,\"endCursor\":null}}}}";

    private InvoiceSyncService serviceAnswering(String response) {
        InvoiceSyncService service = new InvoiceSyncService();
        IShopifyService shopify = mock(IShopifyService.class);
        when(shopify.getNewOrders(anyString(), any())).thenReturn(response);
        ReflectionTestUtils.setField(service, "shopifyService", shopify);
        return service;
    }

    @Test
    void permisoDenegadoEsUnErrorNoCeroPedidos() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> serviceAnswering(DENIED).syncOrdersToPOS("2026-11-12T05:00:00Z"));
        assertTrue(e.getMessage().contains("not approved to access the Order object"));
    }

    @Test
    void sinPedidosNoEsError() {
        assertDoesNotThrow(() -> serviceAnswering(EMPTY).syncOrdersToPOS("2026-11-12T05:00:00Z"));
    }

    @Test
    void errorEnLaConsultaNoAvanzaLaFecha() throws Exception {
        InvoiceSyncService service = mock(InvoiceSyncService.class);
        doThrow(new IllegalStateException("Shopify rechazó la consulta de pedidos: not approved"))
                .when(service).syncOrdersToPOS(anyString());
        Sys_general_configRepository config = mock(Sys_general_configRepository.class);
        when(config.getLastSyncDate()).thenReturn("2026-11-12T05:00:00Z");

        InvoiceSyncJob job = new InvoiceSyncJob();
        ReflectionTestUtils.setField(job, "invoiceSyncService", service);
        ReflectionTestUtils.setField(job, "posClientService", mock(PosClientService.class));
        ReflectionTestUtils.setField(job, "configRepository", config);
        ReflectionTestUtils.setField(job, "chargeItemsService", mock(ChargeItemsService.class));

        job.execute();

        verify(config, never()).updateLastSyncDate(anyString());
    }
}
