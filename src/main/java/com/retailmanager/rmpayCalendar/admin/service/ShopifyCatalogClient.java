package com.retailmanager.rmpayCalendar.admin.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpayCalendar.models.ShopifyResponse;
import com.retailmanager.rmpayCalendar.services.services.Shopify.ShopifyGraphQLClient;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Lecturas de catálogo de Shopify que el sync original no tenía:
 * listado completo de variantes (paginado) y búsqueda de variante por código de barras.
 * Depende de: ShopifyGraphQLClient.execute(query, variables).
 * No modifica: ShopifyService.
 */
@Slf4j
@Service
public class ShopifyCatalogClient {

    private static final int PAGE_SIZE = 200;

    private static final String VARIANTS_QUERY = """
            query catalog($cursor: String) {
              productVariants(first: %d, after: $cursor) {
                pageInfo { hasNextPage endCursor }
                nodes {
                  id
                  sku
                  barcode
                  price
                  title
                  inventoryQuantity
                  inventoryItem { id }
                  product {
                    id
                    title
                    handle
                    status
                    variantsCount { count }
                  }
                }
              }
            }
            """.formatted(PAGE_SIZE);

    private static final String BARCODE_QUERY = """
            query byBarcode($q: String!) {
              productVariants(first: 1, query: $q) {
                nodes { id product { id } inventoryItem { id } }
              }
            }
            """;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ShopifyGraphQLClient client;

    @Data
    public static class ShopifyVariant {
        private String productId;
        private String variantId;
        private String inventoryItemId;
        private String handle;
        private String productTitle;
        private String variantTitle;
        private String status;
        private String sku;
        private String barcode;
        private BigDecimal price;
        private Integer quantity;
        private Integer variantCount;
    }

    /** Descarga todas las variantes de la tienda. */
    public List<ShopifyVariant> fetchAllVariants(IntConsumer onProgress) {
        List<ShopifyVariant> result = new ArrayList<>();
        String cursor = null;
        boolean hasNext = true;

        while (hasNext) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("cursor", cursor);
            JsonNode data = executeWithThrottle(VARIANTS_QUERY, variables).path("data").path("productVariants");

            for (JsonNode node : data.path("nodes")) {
                ShopifyVariant v = new ShopifyVariant();
                v.setVariantId(node.path("id").asText(null));
                v.setSku(textOrNull(node.path("sku")));
                v.setBarcode(textOrNull(node.path("barcode")));
                v.setPrice(node.path("price").isMissingNode() || node.path("price").isNull()
                        ? null : new BigDecimal(node.path("price").asText("0")));
                v.setVariantTitle(textOrNull(node.path("title")));
                v.setQuantity(node.path("inventoryQuantity").isNumber() ? node.path("inventoryQuantity").asInt() : null);
                v.setInventoryItemId(node.path("inventoryItem").path("id").asText(null));
                JsonNode product = node.path("product");
                v.setProductId(product.path("id").asText(null));
                v.setProductTitle(textOrNull(product.path("title")));
                v.setHandle(textOrNull(product.path("handle")));
                v.setStatus(textOrNull(product.path("status")));
                v.setVariantCount(product.path("variantsCount").path("count").isNumber()
                        ? product.path("variantsCount").path("count").asInt() : null);
                result.add(v);
            }
            onProgress.accept(result.size());

            hasNext = data.path("pageInfo").path("hasNextPage").asBoolean(false);
            cursor = data.path("pageInfo").path("endCursor").asText(null);
            if (hasNext && cursor == null) {
                throw new IllegalStateException("Shopify indicó más páginas pero no envió cursor");
            }
        }
        log.info("SHOPIFY_CATALOG | variants={}", result.size());
        return result;
    }

    /** Busca una variante por código de barras; null si no existe o el código está vacío. */
    public ShopifyResponse findByBarcode(String barcode) {
        String code = barcode == null ? "" : barcode.trim();
        if (code.isEmpty()) {
            return null;
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("q", "barcode:\"" + code.replace("\"", "") + "\"");
        JsonNode nodes = executeWithThrottle(BARCODE_QUERY, variables).path("data").path("productVariants").path("nodes");
        if (!nodes.isArray() || nodes.isEmpty()) {
            return null;
        }
        JsonNode node = nodes.get(0);
        ShopifyResponse response = new ShopifyResponse();
        response.setVariantId(node.path("id").asText());
        response.setProductId(node.path("product").path("id").asText());
        response.setInventoryItemId(node.path("inventoryItem").path("id").asText());
        return response;
    }

    private JsonNode executeWithThrottle(String query, Map<String, Object> variables) {
        for (int attempt = 1; ; attempt++) {
            try {
                JsonNode root = mapper.readTree(client.execute(query, variables));
                JsonNode errors = root.path("errors");
                if (errors.isArray() && !errors.isEmpty()) {
                    boolean throttled = errors.toString().contains("THROTTLED");
                    if (throttled && attempt < 6) {
                        Thread.sleep(2000L * attempt);
                        continue;
                    }
                    throw new IllegalStateException("Shopify devolvió errores: " + errors);
                }
                return root;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException("Error consultando Shopify: " + e.getMessage(), e);
            }
        }
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String text = node.asText();
        return text.isEmpty() ? null : text;
    }
}
