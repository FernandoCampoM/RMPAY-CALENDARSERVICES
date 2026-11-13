package com.retailmanager.rmpayCalendar.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.retailmanager.rmpayCalendar.db2.entity.CatalogMatch;
import com.retailmanager.rmpayCalendar.db2.repository.CatalogMatchRepository;
import com.retailmanager.rmpayCalendar.enums.MatchStatus;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

/**
 * Crear en RM desde Shopify conserva el SKU y el código de barras exactos (ceros a la izquierda
 * incluidos): la factura de un pedido manda a RM el SKU de Shopify tal cual, y RM debe encontrarlo.
 */
class CreateInRmCodeTest {

    @Test
    void rmCodeConservaLosCerosYNormalizeCodeSigueComparandoSinEllos() {
        assertEquals("0085529100134", MatchEngine.rmCode(" '0085529100134 "));
        assertEquals("AB-12x", MatchEngine.rmCode("AB-12 x"));
        assertEquals("", MatchEngine.rmCode(null));
        assertEquals("85529100134", MatchEngine.normalizeCode("0085529100134"));
    }

    @Test
    void elCodigoDeBarrasSeCreaConSusCeros() throws Exception {
        PosProductWriter writer = new PosProductWriter();
        PosClientService pos = mock(PosClientService.class);
        ReflectionTestUtils.setField(writer, "posClientService", pos);
        ReflectionTestUtils.setField(writer, "rmWebFlagWriter", mock(RmWebFlagWriter.class));
        CatalogMatch row = new CatalogMatch();
        row.setShopifyTitle("POPCORN CARAMELO");
        row.setShopifyVariantCount(1);
        row.setShopifyBarcode("0085529100134");
        row.setShopifyPrice(new BigDecimal("3.99"));

        writer.create(row, "0085529100134");

        verify(pos).createItem("0085529100134", "POPCORN CARAMELO", new BigDecimal("3.99"), "0085529100134");
    }

    @Test
    void crearEnRmUsaElSkuExactoDeShopify() throws Exception {
        CatalogMatch row = new CatalogMatch();
        row.setId(1L);
        row.setShopifyVariantId("gid://shopify/ProductVariant/1");
        row.setShopifySku("0085529100134");
        row.setShopifyBarcode("0085529100134");
        row.setShopifyTitle("POPCORN CARAMELO");
        row.setShopifyPrice(new BigDecimal("3.99"));
        row.setStatus(MatchStatus.SIN_MATCH);
        CatalogMatchRepository repo = mock(CatalogMatchRepository.class);
        when(repo.findAllById(any())).thenReturn(List.of(row));
        PosProductWriter writer = mock(PosProductWriter.class);
        when(writer.isConfigured()).thenReturn(true);
        when(writer.create(any(), any())).thenAnswer(i -> i.getArgument(1));
        ReconciliationService service = new ReconciliationService();
        ReflectionTestUtils.setField(service, "matchRepository", repo);
        ReflectionTestUtils.setField(service, "posProductWriter", writer);
        ReflectionTestUtils.setField(service, "audit", mock(AuditService.class));

        service.createInRm(List.of(1L), "test", new AdminJobRunner.Job("Crear en RM", "test"));

        verify(writer).create(eq(row), eq("0085529100134"));
        assertEquals("0085529100134", row.getRmProductCode());
    }
}
