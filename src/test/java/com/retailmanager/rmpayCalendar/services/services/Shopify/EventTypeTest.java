package com.retailmanager.rmpayCalendar.services.services.Shopify;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Estado con que se guarda un pedido en ProcessedOrder (InvoiceSyncService.resolveEventType),
 * tanto para pedidos de la consulta GraphQL como para el payload REST del webhook.
 */
class EventTypeTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
        "GraphQL pagado          | {\"displayFinancialStatus\":\"PAID\",\"cancelledAt\":null}                         | PAID",
        "GraphQL cancelado       | {\"displayFinancialStatus\":\"PAID\",\"cancelledAt\":\"2026-11-09T10:00:00Z\"}     | CANCELLED",
        "GraphQL reembolsado     | {\"displayFinancialStatus\":\"REFUNDED\"}                                          | REFUNDED",
        "webhook pagado          | {\"financial_status\":\"paid\",\"cancelled_at\":null}                              | PAID",
        "webhook pago parcial    | {\"financial_status\":\"partially_paid\"}                                          | PARTIALLY_PAID",
        "webhook cancelado       | {\"financial_status\":\"paid\",\"cancelled_at\":\"2026-11-09T10:00:00-04:00\"}     | CANCELLED",
        "sin estado              | {}                                                                                 | UNKNOWN"
    })
    void resuelveElEstado(String name, String json, String expected) throws Exception {
        String result = ReflectionTestUtils.invokeMethod(new InvoiceSyncService(), "resolveEventType",
                new ObjectMapper().readTree(json.trim()));
        assertEquals(expected, result);
    }
}
