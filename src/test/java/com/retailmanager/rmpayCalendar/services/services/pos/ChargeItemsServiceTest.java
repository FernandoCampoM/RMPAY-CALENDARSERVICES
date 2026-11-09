package com.retailmanager.rmpayCalendar.services.services.pos;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Alta automática en RM de los productos de envío y propina (ChargeItemsService)
 * y llamada a ProdCreateNewItem (PosClientService.createProduct). Sin Spring ni red.
 */
class ChargeItemsServiceTest {

    private ChargeItemsService service;
    private PosClientService pos;

    @BeforeEach
    void setUp() {
        service = new ChargeItemsService();
        pos = mock(PosClientService.class);
        ReflectionTestUtils.setField(service, "posClientService", pos);
        ReflectionTestUtils.setField(service, "shippingItemCode", "FLETE");
        ReflectionTestUtils.setField(service, "tipItemCode", "PROPINA");
    }

    @Test
    void creaLosQueFaltanExentosYConPrecioCero() throws Exception {
        when(pos.productExists("FLETE")).thenReturn(false);
        when(pos.productExists("PROPINA")).thenReturn(true);

        service.ensureChargeItems();

        verify(pos).createProduct("FLETE", "ENVIO SHOPIFY", 0, 0, 0);
        verify(pos, never()).createProduct(org.mockito.ArgumentMatchers.eq("PROPINA"), anyString(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void unaVezConfirmadosNoSeVuelvenAConsultar() throws Exception {
        when(pos.productExists(anyString())).thenReturn(true);

        service.ensureChargeItems();
        service.ensureChargeItems();

        verify(pos, times(1)).productExists("FLETE");
        verify(pos, times(1)).productExists("PROPINA");
    }

    @Test
    void siRmNoRespondeSeReintentaEnLaSiguienteLlamada() throws Exception {
        when(pos.productExists(anyString())).thenThrow(new RuntimeException("Connection refused"));

        service.ensureChargeItems();
        service.ensureChargeItems();

        verify(pos, times(2)).productExists("FLETE");
        verify(pos, never()).createProduct(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void siLaCreacionFallaSeReintentaDespues() throws Exception {
        when(pos.productExists(anyString())).thenReturn(false);
        doThrow(new RuntimeException("rechazado")).when(pos).createProduct(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble());

        service.ensureChargeItems();
        service.ensureChargeItems();

        verify(pos, times(2)).createProduct("FLETE", "ENVIO SHOPIFY", 0, 0, 0);
    }

    @Test
    void enSimulacionNoSeCrean() throws Exception {
        ReflectionTestUtils.setField(service, "invoiceDryRun", true);
        when(pos.productExists(anyString())).thenReturn(false);

        service.ensureChargeItems();

        verify(pos, never()).createProduct(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void createProductLlamaAProdCreateNewItem() throws Exception {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        PosClientService client = new PosClientService(rest);
        ReflectionTestUtils.setField(client, "baseUrl", "http://rm");

        server.expect(requestTo("http://rm/cse.api.v1/ProdCreateNewItem?ItemCode=FLETE&Description=ENVIO+SHOPIFY&Price=0.0&Cost=0&Tax1=0.0&Tax2=0.0"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"success\":true,\"message\":\"Created\",\"status\":201}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://rm/cse.api.v1/ProdCreateNewItem?ItemCode=MAL&Description=X&Price=0.0&Cost=0&Tax1=0.0&Tax2=0.0"))
                .andRespond(withSuccess("{\"success\":false,\"message\":\"Item exists\",\"status\":409}", MediaType.APPLICATION_JSON));

        client.createProduct("FLETE", "ENVIO SHOPIFY", 0, 0, 0);
        Assertions.assertThrows(RuntimeException.class, () -> client.createProduct("MAL", "X", 0, 0, 0));
        server.verify();
    }
}
