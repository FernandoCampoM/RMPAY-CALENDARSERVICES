package com.retailmanager.rmpayCalendar.services.services.Shopify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.retailmanager.rmpayCalendar.db2.repository.ProcessedOrderRepository;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

/**
 * Vista previa de factura (InvoiceSyncService.previewInvoice) y consulta de producto en RM
 * (PosClientService.productExists). Sin Spring, sin base de datos y sin red.
 */
class InvoicePreviewTest {

    private static final String ORDER = "{\"data\":{\"order\":{\"id\":\"gid://shopify/Order/1\",\"name\":\"#1001\","
            + "\"createdAt\":\"2026-11-03T10:00:00Z\",\"displayFinancialStatus\":\"PAID\",\"taxesIncluded\":false,"
            + "\"subtotalPriceSet\":{\"shopMoney\":{\"amount\":\"45.00\"}},"
            + "\"totalPriceSet\":{\"shopMoney\":{\"amount\":\"58.17\"}},"
            + "\"totalTaxSet\":{\"shopMoney\":{\"amount\":\"5.18\"}},"
            + "\"taxLines\":[{\"title\":\"PR State Tax\",\"rate\":0.105,\"priceSet\":{\"shopMoney\":{\"amount\":\"4.73\"}}},"
            + "{\"title\":\"PR Municipal Tax\",\"rate\":0.01,\"priceSet\":{\"shopMoney\":{\"amount\":\"0.45\"}}}],"
            + "\"lineItems\":{\"edges\":[{\"node\":{\"name\":\"P\",\"quantity\":1,"
            + "\"originalUnitPriceSet\":{\"shopMoney\":{\"amount\":\"50.00\"}},\"variant\":{\"sku\":\"S\"}}}]}}}}";

    private InvoiceSyncService service(IShopifyService shopify, PosClientService pos) {
        InvoiceSyncService service = new InvoiceSyncService();
        ProcessedOrderRepository repository = mock(ProcessedOrderRepository.class);
        ReflectionTestUtils.setField(service, "shippingItemCode", "FLETE");
        ReflectionTestUtils.setField(service, "shopifyService", shopify);
        ReflectionTestUtils.setField(service, "posClientService", pos);
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "tipItemCode", "PROPINA");
        ReflectionTestUtils.setField(service, "failedOrderRepository",
                mock(com.retailmanager.rmpayCalendar.db2.repository.FailedOrderRepository.class));
        return service;
    }

    @Test
    void previewArmaLaFacturaSinEnviarla() throws Exception {
        IShopifyService shopify = mock(IShopifyService.class);
        PosClientService pos = mock(PosClientService.class);
        when(shopify.getOrderById("gid://shopify/Order/1")).thenReturn(ORDER);

        Map<String, Object> result = service(shopify, pos).previewInvoice("gid://shopify/Order/1");

        assertEquals(true, result.get("ruleOk"));
        assertEquals(true, result.get("wouldSend"));
        assertEquals(false, result.get("alreadyProcessed"));
        @SuppressWarnings("unchecked")
        Map<String, Object> invoice = (Map<String, Object>) result.get("invoice");
        assertEquals(58.17, (double) invoice.get("Total"), 0.001);
        assertEquals(0.45, (double) invoice.get("CityTax"), 0.001);
        verify(pos, never()).sendInvoiceToPOS(any());
    }

    @Test
    void previewDeOrdenInexistente() {
        IShopifyService shopify = mock(IShopifyService.class);
        when(shopify.getOrderById(any())).thenReturn("{\"data\":{\"order\":null}}");

        assertThrows(IllegalArgumentException.class,
                () -> service(shopify, mock(PosClientService.class)).previewInvoice("gid://shopify/Order/9"));
    }

    @Test
    void productExistsInterpretaLaRespuestaDeRm() throws Exception {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        PosClientService pos = new PosClientService(rest);
        ReflectionTestUtils.setField(pos, "baseUrl", "http://rm");

        server.expect(requestTo("http://rm/cse.api.v1/ProductInfo?ItemCode=FLETE"))
                .andRespond(withSuccess("{\"ItemCode\":\"FLETE\",\"Price\":0}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://rm/cse.api.v1/ProductInfo?ItemCode=NOEXISTE"))
                .andRespond(withSuccess("{\"success\":false,\"message\":\"NoMatch\",\"status\":404}", MediaType.APPLICATION_JSON));

        assertTrue(pos.productExists("FLETE"));
        assertEquals(false, pos.productExists("NOEXISTE"));
        server.verify();
    }
}
