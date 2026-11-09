package com.retailmanager.rmpayCalendar.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.retailmanager.rmpayCalendar.db2.entity.CatalogMatch;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

/**
 * "Crear en RM" de la conciliación (PosProductWriter): ProdCreateNewItem y marca web en POSDPS.
 * Sin Spring, base de datos ni red.
 */
class PosProductWriterTest {

    private PosProductWriter writer;
    private PosClientService pos;
    private RmWebFlagWriter webFlag;

    @BeforeEach
    void setUp() {
        writer = new PosProductWriter();
        pos = mock(PosClientService.class);
        webFlag = mock(RmWebFlagWriter.class);
        ReflectionTestUtils.setField(writer, "posClientService", pos);
        ReflectionTestUtils.setField(writer, "rmWebFlagWriter", webFlag);
    }

    private static CatalogMatch row(String title, String variant, int variants, String barcode, String price) {
        CatalogMatch row = new CatalogMatch();
        row.setShopifyTitle(title);
        row.setShopifyVariantTitle(variant);
        row.setShopifyVariantCount(variants);
        row.setShopifyBarcode(barcode);
        row.setShopifyPrice(new BigDecimal(price));
        return row;
    }

    @Test
    void creaElProductoConPrecioYBarcodeYLoMarcaWeb() throws Exception {
        when(pos.productExists("SKU-1")).thenReturn(false);

        String code = writer.create(row("Camisa", "Roja / M", 3, "7501234567890", "19.99"), "SKU-1");

        assertEquals("SKU-1", code);
        verify(pos).createItem("SKU-1", "Camisa Roja / M", new BigDecimal("19.99"), "7501234567890");
        verify(webFlag).markAsWeb("SKU-1");
    }

    @Test
    void siYaExisteEnRmSoloLoMarcaWeb() throws Exception {
        when(pos.productExists("SKU-2")).thenReturn(true);

        writer.create(row("Gorra", null, 1, null, "9.50"), "SKU-2");

        verify(pos, never()).createItem(anyString(), anyString(), any(), any());
        verify(webFlag).markAsWeb("SKU-2");
    }

    @Test
    void respetaLosLimitesDeRm() throws Exception {
        String longTitle = "X".repeat(120);
        writer.create(row(longTitle, null, 1, "1".repeat(50), "1.00"), "SKU-3");

        ArgumentCaptor<String> description = ArgumentCaptor.forClass(String.class);
        verify(pos).createItem(eq("SKU-3"), description.capture(), any(), eq(""));
        assertEquals(90, description.getValue().length());
    }

    @Test
    void codigoDeMasDe30CaracteresNoSeEnvia() {
        assertThrows(IllegalStateException.class,
                () -> writer.create(row("A", null, 1, null, "1.00"), "C".repeat(31)));
        verifyNoInteractions(pos, webFlag);
    }

    @Test
    void createItemLlamaAProdCreateNewItem() throws Exception {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        PosClientService client = new PosClientService(rest);
        ReflectionTestUtils.setField(client, "baseUrl", "http://rm");

        server.expect(requestTo("http://rm/cse.api.v1/ProdCreateNewItem?ItemCode=SKU-1&Description=Camisa+Roja&Price=19.99&Cost=0&Barcode=750123"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"success\":true,\"message\":\"Created\",\"status\":201}", MediaType.APPLICATION_JSON));

        client.createItem("SKU-1", "Camisa Roja", new BigDecimal("19.99"), "750123");
        server.verify();
    }
}
