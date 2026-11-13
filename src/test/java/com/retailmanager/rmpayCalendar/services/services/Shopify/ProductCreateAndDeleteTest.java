package com.retailmanager.rmpayCalendar.services.services.Shopify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.retailmanager.rmpayCalendar.admin.service.AuditService;
import com.retailmanager.rmpayCalendar.admin.service.SyncSettingsService;
import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncAdminRepository;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncRepository;
import com.retailmanager.rmpayCalendar.db2.repository.Sys_general_configRepository;
import com.retailmanager.rmpayCalendar.enums.SyncMaster;
import com.retailmanager.rmpayCalendar.models.ShopifyResponse;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

/**
 * Alta de productos de RM en Shopify (ShopifyService.createProduct) con campos vacíos, y
 * detección de productos borrados en RM (ProductSyncJob) con catálogos de más de 2100 productos.
 */
class ProductCreateAndDeleteTest {

    private static final String PRODUCT_SET_OK = "{\"data\":{\"productSet\":{\"product\":{\"id\":\"gid://shopify/Product/1\","
            + "\"variants\":{\"edges\":[{\"node\":{\"id\":\"gid://shopify/ProductVariant/2\",\"inventoryItem\":{\"id\":\"gid://shopify/InventoryItem/3\"}}}]}},"
            + "\"userErrors\":[]}}}";

    private static PosProduct product(String code, String webDesc, String category, String department) {
        PosProduct p = new PosProduct();
        p.setProductCode(code);
        p.setProductName("NUTELLA 13OZ");
        p.setPrice(new BigDecimal("5.99"));
        p.setBarCode("0000000988639");
        p.setWebDesc(webDesc);
        p.setCategory(category);
        p.setDepartment(department);
        return p;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createAndCaptureInput(PosProduct product) throws Exception {
        ShopifyService service = new ShopifyService();
        ShopifyGraphQLClient client = mock(ShopifyGraphQLClient.class);
        when(client.execute(anyString(), any())).thenReturn(PRODUCT_SET_OK);
        ReflectionTestUtils.setField(service, "client", client);

        ShopifyResponse response = service.createProduct(product);
        assertEquals("gid://shopify/InventoryItem/3", response.getInventoryItemId());

        ArgumentCaptor<Map<String, Object>> variables = ArgumentCaptor.forClass(Map.class);
        verify(client).execute(anyString(), variables.capture());
        return (Map<String, Object>) variables.getValue().get("input");
    }

    @Test
    void creaProductoSinDescripcionWeb() throws Exception {
        Map<String, Object> input = createAndCaptureInput(product("0000000988639", null, "GROCERY", "ALIMENTOS"));

        assertEquals("NUTELLA 13OZ", input.get("title"));
        assertFalse(input.containsKey("descriptionHtml"));
        assertEquals("GROCERY", input.get("productType"));
        assertEquals("ALIMENTOS", input.get("vendor"), "la marca es el departamento de RM");
        assertEquals(List.of("ALIMENTOS"), input.get("tags"));
    }

    @Test
    void creaProductoSinCategoriaNiDepartamento() throws Exception {
        Map<String, Object> input = createAndCaptureInput(product("X1", "<p>Desc</p>", null, " "));

        assertEquals("<p>Desc</p>", input.get("descriptionHtml"));
        assertFalse(input.containsKey("productType"));
        assertFalse(input.containsKey("vendor"));
        assertFalse(input.containsKey("tags"));
    }

    @Test
    void detectaBorradosConMasDe2100ProductosEnRm() {
        List<PosProduct> rm = new ArrayList<>();
        for (int i = 0; i < 2500; i++) {
            rm.add(product("P" + i, null, "C", "D"));
        }
        ProductSync linkedOk = new ProductSync();
        linkedOk.setProductCode("P10");
        ProductSync linkedGone = new ProductSync();
        linkedGone.setProductCode("YA-NO-ESTA");

        ProductSyncRepository repo = mock(ProductSyncRepository.class);
        when(repo.findAllByDeletedFalse()).thenReturn(List.of(linkedOk, linkedGone));
        PosClientService pos = mock(PosClientService.class);
        when(pos.getAllProducts()).thenReturn(rm);
        SyncSettingsService settings = mock(SyncSettingsService.class);
        when(settings.getMaster()).thenReturn(SyncMaster.RM);
        when(settings.getMaxDeletePercent()).thenReturn(100);
        ProductSyncAdminRepository adminRepo = mock(ProductSyncAdminRepository.class);
        when(adminRepo.countByDeletedFalse()).thenReturn(2L);
        ProductSyncService syncService = mock(ProductSyncService.class);
        Sys_general_configRepository config = mock(Sys_general_configRepository.class);
        when(config.getIsFirstRun()).thenReturn("false");

        ProductSyncJob job = new ProductSyncJob();
        ReflectionTestUtils.setField(job, "productSyncService", syncService);
        ReflectionTestUtils.setField(job, "productSyncRepository", repo);
        ReflectionTestUtils.setField(job, "posClientService", pos);
        ReflectionTestUtils.setField(job, "configRepository", config);
        ReflectionTestUtils.setField(job, "syncSettings", settings);
        ReflectionTestUtils.setField(job, "productSyncAdminRepository", adminRepo);
        ReflectionTestUtils.setField(job, "auditService", mock(AuditService.class));

        job.execute();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProductSync>> deleted = ArgumentCaptor.forClass(List.class);
        verify(syncService).processDeletedProducts(deleted.capture());
        assertEquals(List.of(linkedGone), deleted.getValue());
        verify(repo, never()).findAllByProductCodeNotInAndDeletedFalse(any());
    }
}
