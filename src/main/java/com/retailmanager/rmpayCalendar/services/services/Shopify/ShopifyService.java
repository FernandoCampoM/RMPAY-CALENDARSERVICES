package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.models.ShopifyResponse;

@Service
public class ShopifyService implements IShopifyService {
    @Autowired
    private  ShopifyGraphQLClient client;
    @Value("${shopify.location-id}")
private String LOCATION_ID;
    private final Semaphore semaphore = new Semaphore(2);

    @Override
public ShopifyResponse createProduct(PosProduct product) {

    String mutation = String.format("""
        mutation {
          productCreate(input: {
            title: "%s",
            productType: "%s",
            vendor: "Mi Empresa",
            tags: ["%s"],
            variants: [{
              price: "%s",
              sku: "%s",
              barcode: "%s",
              inventoryManagement: SHOPIFY
            }]
          }) {
            product {
              id
              variants(first: 1) {
                edges {
                  node {
                    id
                    inventoryItem {
                      id
                    }
                  }
                }
              }
            }
          }
        }
        """,
        escape(product.getProductName()),
        escape(product.getCategory()),
        escape(product.getDepartment()),
        product.getPrice(),
        product.getProductCode(),
        product.getBarCode()
    );

    try {
        String response = executeWithRetry(mutation);

        return parseResponse(response); // 🔥 CLAVE

    } catch (Exception e) {
        throw new RuntimeException("Error creando producto en Shopify", e);
    }
}

    @Override
public void updateProduct(PosProduct product, ProductSync sync) {

    String mutation = String.format("""
        mutation {
          productVariantUpdate(input: {
            id: "%s",
            price: "%s"
          }) {
            productVariant {
              id
            }
          }
        }
        """,
        sync.getShopifyVariantId(),
        product.getPrice()
    );

    try {
        executeWithRetry(mutation);
    } catch (Exception e) {
        throw new RuntimeException("Error actualizando producto", e);
    }
}

    @Override
public void updateInventory(String inventoryItemId, int quantity) {

    String mutation = String.format("""
        mutation {
          inventorySetQuantities(input: {
            reason: "correction",
            setQuantities: [{
              inventoryItemId: "%s",
              locationId: "%s",
              quantity: %d
            }]
          }) {
            inventoryAdjustmentGroup {
              createdAt
            }
          }
        }
        """,
        inventoryItemId,
        LOCATION_ID,
        quantity
    );

    try {
        executeWithRetry(mutation);
    } catch (Exception e) {
        throw new RuntimeException("Error actualizando inventario", e);
    }
}
@Override
public ShopifyResponse findBySku(String sku) {

    String query = String.format("""
    {
      productVariants(first: 1, query: "sku:%s") {
        edges {
          node {
            id
            product {
              id
            }
            inventoryItem {
              id
            }
          }
        }
      }
    }
    """, sku);

    try {

        String response = executeWithRetry(query);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        JsonNode edges = root
                .path("data")
                .path("productVariants")
                .path("edges");

        // 🔥 SI NO EXISTE
        if (edges.isEmpty()) {
            return null;
        }

        JsonNode node = edges.get(0).path("node");

        ShopifyResponse result = new ShopifyResponse();

        result.setVariantId(node.path("id").asText());
        result.setProductId(node.path("product").path("id").asText());
        result.setInventoryItemId(node.path("inventoryItem").path("id").asText());

        return result;

    } catch (Exception e) {
        throw new RuntimeException("Error buscando producto por SKU", e);
    }
}
    private String escape(String value) {
    return value == null ? "" : value.replace("\"", "\\\"");
}


public String executeControlled(String query) throws InterruptedException {

    semaphore.acquire();

    try {
        return client.execute(query);
    } finally {
        Thread.sleep(300);
        semaphore.release();
    }
}
@Retryable(
    retryFor = { RuntimeException.class, IOException.class },
    maxAttempts = 5,
    backoff = @Backoff(delay = 2000, multiplier = 2)
)
public String executeWithRetry(String query) {
    try {
        return executeControlled(query);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
    }
}
private ShopifyResponse parseResponse(String json) {

    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        if (root.has("errors")) {
    throw new RuntimeException("Error en Shopify: " + root.get("errors"));
}
        JsonNode node = root
            .path("data")
            .path("productCreate")
            .path("product");

        String productId = node.path("id").asText();

        JsonNode variantNode = node
            .path("variants")
            .path("edges")
            .get(0)
            .path("node");

        String variantId = variantNode.path("id").asText();
        String inventoryItemId = variantNode
                .path("inventoryItem")
                .path("id")
                .asText();

        ShopifyResponse response = new ShopifyResponse();
        response.setProductId(productId);
        response.setVariantId(variantId);
        response.setInventoryItemId(inventoryItemId);

        return response;

    } catch (Exception e) {
        throw new RuntimeException("Error parseando respuesta Shopify", e);
    }
}
}
