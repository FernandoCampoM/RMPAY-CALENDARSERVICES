package com.retailmanager.rmpayCalendar.services.services.Shopify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Filtro de fecha de la consulta de pedidos: la fecha va entre comillas para que Shopify use la
 * hora completa (sin comillas solo toma el día y, de noche en UTC, pide pedidos del día siguiente).
 */
class OrderDateFilterTest {

    @Test
    void laFechaVaEntreComillas() {
        assertEquals("created_at:>'2026-11-13T03:06:18Z'", ShopifyService.ordersCreatedAfterFilter("2026-11-13T03:06:18Z"));
        assertEquals("created_at:>'2026-11-12T05:00:00Z'", ShopifyService.ordersCreatedAfterFilter(" '2026-11-12T05:00:00Z' "));
    }

    @Test
    @SuppressWarnings("unchecked")
    void ambasConsultasDePedidosUsanElFiltro() throws Exception {
        ShopifyService service = new ShopifyService();
        ShopifyGraphQLClient client = mock(ShopifyGraphQLClient.class);
        when(client.execute(anyString(), any())).thenReturn("{}");
        ReflectionTestUtils.setField(service, "client", client);

        service.getNewOrders("2026-11-13T03:06:18Z");
        service.getNewOrders("2026-11-13T03:06:18Z", null);

        ArgumentCaptor<Map<String, Object>> variables = ArgumentCaptor.forClass(Map.class);
        verify(client, times(2)).execute(anyString(), variables.capture());
        List<Map<String, Object>> calls = variables.getAllValues();
        assertEquals("created_at:>'2026-11-13T03:06:18Z'", calls.get(0).get("query"));
        assertEquals("created_at:>'2026-11-13T03:06:18Z'", calls.get(1).get("query"));
    }
}
