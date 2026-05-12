package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import com.retailmanager.rmpayCalendar.services.DTO.ShopifyPublication;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class ShopifyService implements IShopifyService {
    @Autowired
    private  ShopifyGraphQLClient client;
    //@Value("${shopify.location-id}")
private String LOCATION_ID;
    private final Semaphore semaphore = new Semaphore(2);
    private List<ShopifyPublication> publications = new ArrayList<>();
    @PostConstruct
    public void init() {
        try {
            this.LOCATION_ID=getShopLocationId();
            System.out.println("✅ Location inicializado");
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo inicializar el token al arranque");
        }
    }

    @Override
public ShopifyResponse createProduct(PosProduct product) {
  try {

        String mutation = """
        mutation productSet($input: ProductSetInput!, $sync: Boolean!) {
          productSet(synchronous: $sync, input: $input) {
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
            userErrors {
              field
              message
            }
          }
        }
        """;

        Map<String, Object> variables = Map.of(
            "sync", true,
            "input", Map.of(
                "title", product.getProductName(),
                "descriptionHtml", product.getWebDesc(),
                "productType", product.getCategory(),
                "vendor", "Mi Empresa",
                "tags", List.of(product.getDepartment()),
                "productOptions", List.of(
                    Map.of(
                        "name", "Title",
                        "values", List.of(
                            Map.of("name", product.getProductName())
                        )
                    )
                ),
                "variants", List.of(
                    Map.of(
                        "price", product.getPrice(),
                        "barcode", product.getBarCode() == null ? "" : product.getBarCode(),
                        "inventoryItem", Map.of(
                            "sku", product.getProductCode(),
                            "tracked", true
                        ),
                        "optionValues", List.of(
                            Map.of(
                                "optionName", "Title",
                                "name", product.getProductName()
                            )
                        )
                    )
                )
            )
        );
        // 🔥 AQUÍ USAS EL NUEVO MÉTODO
        String response = client.execute(mutation, variables);

        return parseProductSetResponse(response);

    } catch (Exception e) {
        System.out.println("Error en com.retailmanager.rmpayCalendar.services.services.Shopify.ShopifyService.createProduct: " + e.getMessage());
        log.error("Error en com.retailmanager.rmpayCalendar.services.services.Shopify.ShopifyService.createProduct", e);
        throw new RuntimeException("Error creando producto en Shopify", e);
    }
}

@Override
public void updateProduct(PosProduct product, ProductSync sync) {
    try {

        String mutation = """
        mutation productSet($input: ProductSetInput!, $sync: Boolean!) {
          productSet(synchronous: $sync, input: $input) {
            product {
              id
              title
              variants(first: 1) {
                edges {
                  node {
                    id
                    price
                    barcode
                  }
                }
              }
            }
            userErrors {
              field
              message
            }
          }
        }
        """;

        Map<String, Object> variables = Map.of(
            "sync", true,
            "input", Map.of(
                "id", sync.getShopifyProductId(),
                "title", product.getProductName(),
                "descriptionHtml", product.getWebDesc(),

                // 🔥 OBLIGATORIO cuando hay variants
                "productOptions", List.of(
                    Map.of(
                        "name", "Title",
                        "values", List.of(
                            Map.of("name", product.getProductName())
                        )
                    )
                ),

                "variants", List.of(
                    Map.of(
                        "id", sync.getShopifyVariantId(),
                        "price", product.getPrice(),
                        "barcode", product.getBarCode() == null ? "" : product.getBarCode(),

                        "optionValues", List.of(
                            Map.of(
                                "optionName", "Title",
                                "name", product.getProductName()
                            )
                        )
                    )
                )
            )
        );
        System.out.println("📦 Datos enviados a Shopify: " + variables);
        System.out.println("📦 Mutation: " + mutation);
        String response = client.execute(mutation, variables);

        validateUserErrors(response);

    } catch (Exception e) {
        System.out.println("Error actualizando producto en Shopify: " + e.getMessage());
        log.error("Error actualizando producto en Shopify", e);
        throw new RuntimeException("Error actualizando producto", e);
    }
}
public void updateVariant(PosProduct product, ProductSync sync) {
    try {

        String mutation = """
        mutation productVariantUpdate($input: ProductVariantInput!) {
          productVariantUpdate(input: $input) {
            productVariant {
              id
              price
              barcode
            }
            userErrors {
              field
              message
            }
          }
        }
        """;

        Map<String, Object> variables = Map.of(
            "input", Map.of(
                "id", sync.getShopifyVariantId(),
                "price", product.getPrice(),
                "barcode", product.getBarCode() == null ? "" : product.getBarCode()
            )
        );

        String response = client.execute(mutation, variables);

        // 🔥 opcional: validar errores
        validateUserErrors(response);

    } catch (Exception e) {
        log.error("Error actualizando variante en Shopify", e);
        throw new RuntimeException("Error actualizando variante", e);
    }
}
private void validateUserErrors(String response) {
    if (response.contains("userErrors") && !response.contains("\"userErrors\":[]")) {
        log.error("Errores en respuesta de Shopify: {}", response);
        throw new RuntimeException("Shopify retornó errores");
    }
}

   @Override
public void updateInventory(String inventoryItemId, int quantity) {

    try {
        if(LOCATION_ID==null){
            LOCATION_ID=getShopLocationId();
        }
        // 1. Obtener inventario actual
        int currentQuantity = getCurrentInventory(inventoryItemId);
        if(currentQuantity == quantity) return;
        // 2. Generar idempotent key
        String idempotentKey = generateIdempotentKey(inventoryItemId);

        String mutation = String.format("""
            mutation {
              inventorySetQuantities(input: {
                name: "available",
                reason: "correction",
                quantities: [{
                  inventoryItemId: "%s",
                  locationId: "%s",
                  quantity: %d,
                  changeFromQuantity: %d
                }]
              }) @idempotent(key: "%s") {
                inventoryAdjustmentGroup {
                  createdAt
                  reason
                  changes {
                    name
                    delta
                  }
                }
                userErrors {
                  field
                  message
                }
              }
            }
            """,
            inventoryItemId,
            LOCATION_ID,
            quantity,
            currentQuantity,
            idempotentKey
        );

        String response = executeWithRetry(mutation);

        // 🔥 Manejo de errores Shopify
        JsonNode root = new ObjectMapper().readTree(response);
        JsonNode errors = root
            .path("data")
            .path("inventorySetQuantities")
            .path("userErrors");

        if (errors.isArray() && errors.size() > 0) {
            throw new RuntimeException("Errores Shopify: " + errors.toString());
        }

    } catch (Exception e) {
        System.out.println("ERROR en com.retailmanager.rmpayCalendar.services.services.Shopify.ShopifyService.updateInventory: "+e.getMessage());
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

private String extractProductId(String json) {

    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        return root
                .path("data")
                .path("productCreate")
                .path("product")
                .path("id")
                .asText();

    } catch (Exception e) {
        log.info("com.retailmanager.rmpayCalendar.services.services.Shopify.ShopifyService.extractProductId", e);
        throw new RuntimeException("Error extrayendo productId", e);
    }
}
private ShopifyResponse parseVariantResponse(String productId, String json) {

    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode node = root
                .path("data")
                .path("productVariantsBulkCreate")
                .path("productVariants")
                .get(0);

        ShopifyResponse response = new ShopifyResponse();
        response.setProductId(productId);
        response.setVariantId(node.path("id").asText());
        response.setInventoryItemId(
                node.path("inventoryItem").path("id").asText()
        );

        return response;

    } catch (Exception e) {
        log.info("com.retailmanager.rmpayCalendar.services.services.Shopify.ShopifyService.parseVariantResponse", e);
        throw new RuntimeException("Error parseando variante", e);
    }
}
private ShopifyResponse parseProductSetResponse(String json) {

    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        JsonNode productNode = root
                .path("data")
                .path("productSet")
                .path("product");

        if (productNode.isMissingNode()) {
            throw new RuntimeException("Error en productSet: " + json);
        }

        String productId = productNode.path("id").asText();

        JsonNode variantNode = productNode
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
        throw new RuntimeException("Error parseando productSet", e);
    }
}
private String toJsonString(String value) {
    return "\"" + value.replace("\"", "\\\"") + "\"";
}
        /**
         * Returns the current available quantity for the given inventory item id.
         * If the inventory item is not found, a RuntimeException is thrown.
         * @param inventoryItemId the Shopify inventory item id
         * @return the current available quantity
         * @throws RuntimeException if the inventory item is not found
         */
public int getCurrentInventory(String inventoryItemId) {
    try {

        String query = """
            query getInventory($id: ID!) {
              inventoryItem(id: $id) {
                inventoryLevels(first: 1) {
                  edges {
                    node {
                      quantities(names: ["available"]) {
                        quantity
                      }
                    }
                  }
                }
              }
            }
        """;

        Map<String, Object> variables = Map.of(
            "id", inventoryItemId
        );

        String response = client.execute(query, variables);

        JsonNode root = new ObjectMapper().readTree(response);

        JsonNode quantityNode = root
            .path("data")
            .path("inventoryItem")
            .path("inventoryLevels")
            .path("edges");

        if (quantityNode.isEmpty()) {
            throw new RuntimeException("No se encontró inventario para el item");
        }

        int quantity = quantityNode
            .get(0)
            .path("node")
            .path("quantities")
            .get(0)
            .path("quantity")
            .asInt();

        return quantity;

    } catch (Exception e) {
        throw new RuntimeException("Error obteniendo inventario", e);
    }
}
private String generateIdempotentKey(String inventoryItemId) {
    return "inv-" + inventoryItemId + "-" + UUID.randomUUID();
}
/**
 * Obtiene una lista de canales de Shopify.
 *
 * @return Una lista de publicaciones de Shopify.
 * @throws RuntimeException Si ocurre un error al obtener la lista de publicaciones.
 */
public List<ShopifyPublication> getPublications() {
    try {

        String query = """
            query getPublications {
              publications(first: 10) {
                edges {
                  node {
                    id
                    name
                  }
                }
              }
            }
        """;

        String response = client.execute(query);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);

        JsonNode edges = root
            .path("data")
            .path("publications")
            .path("edges");

        List<ShopifyPublication> publications = new ArrayList<>();

        for (JsonNode edge : edges) {
            JsonNode node = edge.path("node");

            ShopifyPublication pub = new ShopifyPublication();
            pub.setId(node.path("id").asText());
            pub.setName(node.path("name").asText());

            publications.add(pub);
        }

        return publications;

    } catch (Exception e) {
        throw new RuntimeException("Error obteniendo publications", e);
    }
}
        /**
         * Publica un producto en una publicaci n de Shopify
         * @param productId El ID del producto a publicar
         * @param publicationId El ID de la publicaci n en la que se va a publicar el producto
         */
@Override
public void publishProduct(String productId, String publicationId) {
    try {

        String mutation = """
            mutation publishProduct($id: ID!, $publicationId: ID!) {
              publishablePublish(
                id: $id,
                input: {
                  publicationId: $publicationId
                }
              ) {
                publishable {
                  ... on Product {
                    id
                  }
                }
                userErrors {
                  field
                  message
                }
              }
            }
        """;

        Map<String, Object> variables = Map.of(
            "id", productId,
            "publicationId", publicationId
        );

        String response = client.execute(mutation, variables);

        JsonNode root = new ObjectMapper().readTree(response);

        JsonNode errors = root
            .path("data")
            .path("publishablePublish")
            .path("userErrors");

        if (errors.isArray() && errors.size() > 0) {
            throw new RuntimeException("Error publicando producto: " + errors.toString());
        }

    } catch (Exception e) {
        throw new RuntimeException("Error publicando producto", e);
    }
}

        /**
         * Publica un producto en todos los canales de Shopify
         * @param productId El ID del producto a publicar
         */
        @Override
public void publishProductInAllChannels(String productId) {
    try {

        if(publications == null) {
            publications = getPublications();
        }else if(publications.size() == 0) {
            publications = getPublications();
        }

        for (ShopifyPublication pub : publications) {
            try {
                publishProduct(productId, pub.getId());
            } catch (Exception e) {
                System.out.println("Error publicando en canal: " + pub.getName());
            }
        }

    } catch (Exception e) {
        throw new RuntimeException("Error publicando producto en todos los canales", e);
    }
}

        /**
         * Obtiene la lista de IDs de publicaciones para un producto dado.
         * @param productId El ID del producto del que se desean obtener las publicaciones.
         * @return La lista de IDs de publicaciones del producto.
         * @throws RuntimeException Si ocurre un error al obtener la lista de publicaciones.
         */
        @Override
public List<String> getPublicationIdsByProduct(String productId) {
    try {

        String query = """
            query getProductPublications($id: ID!) {
              product(id: $id) {
                resourcePublications(first: 10) {
                  edges {
                    node {
                      publication {
                        id
                      }
                    }
                  }
                }
              }
            }
        """;

        Map<String, Object> variables = Map.of(
            "id", productId
        );

        String response = client.execute(query, variables);

        JsonNode edges = new ObjectMapper()
            .readTree(response)
            .path("data")
            .path("product")
            .path("resourcePublications")
            .path("edges");

        List<String> publicationIds = new ArrayList<>();

        if (edges.isArray()) {
            for (JsonNode edge : edges) {
                String pubId = edge
                    .path("node")
                    .path("publication")
                    .path("id")
                    .asText();

                if (pubId != null && !pubId.isEmpty()) {
                    publicationIds.add(pubId);
                }
            }
        }

        return publicationIds;

    } catch (Exception e) {
        throw new RuntimeException("Error obteniendo publications del producto", e);
    }
}
public List<ShopifyPublication> getAvailablePublications(){

    if(publications == null) {
        publications = getPublications();
    }else if(publications.size() == 0) {
        publications = getPublications();
    }

    return publications;
}
        /**
         * Obtiene una lista de las 10 últimas órdenes
         * @return Una cadena JSON con la lista de órdenes
         * @throws RuntimeException Si ocurre un error al obtener la lista de órdenes
         */
@Override
public String getNewOrders(String lastSyncDate) {
    try {

        String query = """
            query getOrders($query: String!) {
              orders(first: 50, sortKey: CREATED_AT, reverse: false, query: $query) {
                edges {
                  cursor
                  node {
                    id
                    name
                    createdAt
                    totalPriceSet { shopMoney { amount } }
                    subtotalPriceSet { shopMoney { amount } }
                    totalDiscountsSet { shopMoney { amount } }
                    customer {
                      firstName
                      phone
                    }
                    billingAddress {
                      address1
                    }
                    lineItems(first: 50) {
                      edges {
                        node {
                          name
                          quantity
                          originalUnitPriceSet {
                            shopMoney { amount }
                          }
                          variant {
                            sku 
                          }
                        }
                      }
                    }
                  }
                }
                pageInfo {
                  hasNextPage
                }
              }
            }
        """;

        // 🔥 Filtro de fecha dinámica (IMPORTANTE: con Z)
        String filter = "created_at:>" + lastSyncDate;

        Map<String, Object> variables = Map.of(
            "query", filter
        );

        return client.execute(query, variables);

    } catch (Exception e) {
        throw new RuntimeException("Error obteniendo órdenes nuevas", e);
    }
}

public String getShopLocationId() {
    try {

        String query = """
            query {
              locations(first: 10) {
                edges {
                  node {
                    id
                    name
                  }
                }
              }
            }
        """;

        String response = client.execute(query);

        JsonNode edges = new ObjectMapper()
                .readTree(response)
                .path("data")
                .path("locations")
                .path("edges");

        String fallbackId = null;

        if (edges.isArray()) {
            for (JsonNode edge : edges) {

                JsonNode node = edge.path("node");

                String name = node.path("name").asText();
                String id = node.path("id").asText();

                // 🔥 Guardamos el primero como fallback
                if (fallbackId == null && id != null && !id.isEmpty()) {
                    fallbackId = id;
                }

                // 🔥 Si encontramos el ideal → retornamos
                if ("Shop location".equalsIgnoreCase(name)) {
                    return id;
                }
            }
        }

        // 🔥 Si no existe "Shop location", devolvemos el primero
        if (fallbackId != null) {
            return fallbackId;
        }

        throw new RuntimeException("No se encontraron locations");

    } catch (Exception e) {
        throw new RuntimeException("Error obteniendo Location ID", e);
    }
}

@Override
public ShopifyResponse deleteProduct(ProductSync sync) {

    try {

        // 🔥 VALIDACIÓN DEFENSIVA
        if (sync == null) {
            throw new IllegalArgumentException("ProductSync no puede ser null");
        }

        if (sync.getShopifyProductId() == null || sync.getShopifyProductId().isBlank()) {
            throw new IllegalArgumentException(
                "El producto no tiene shopifyProductId"
            );
        }

        // 🔥 MUTATION GRAPHQL
        String mutation = """
        mutation productDelete($input: ProductDeleteInput!) {
          productDelete(input: $input) {
            deletedProductId
            userErrors {
              field
              message
            }
          }
        }
        """;

        // 🔥 VARIABLES
        Map<String, Object> variables = Map.of(
            "input", Map.of(
                "id", sync.getShopifyProductId()
            )
        );

        log.info(
            "SHOPIFY_DELETE_PRODUCT_START | productCode={} | shopifyProductId={}",
            sync.getProductCode(),
            sync.getShopifyProductId()
        );

        // 🔥 EJECUCIÓN
        String response = client.execute(mutation, variables);

        log.info(
            "SHOPIFY_DELETE_PRODUCT_RESPONSE | productCode={} | response={}",
            sync.getProductCode(),
            response
        );

        // 🔥 PARSEAR RESPUESTA
        ShopifyResponse shopifyResponse = parseDeleteResponse(response);

        // 🔥 VALIDAR userErrors
        if (shopifyResponse.hasErrors()) {

            log.error(
                "SHOPIFY_DELETE_PRODUCT_USER_ERROR | productCode={} | errors={}",
                sync.getProductCode(),
                shopifyResponse.getErrors()
            );
            if (shopifyResponse.getErrors().size() > 0) {
                if(!shopifyResponse.getErrors().get(0).contains("Product does not exist")){
                   throw new RuntimeException(
                shopifyResponse.getErrors().get(0)
            );
                }
            }
        }

        // 🔥 VALIDAR deletedProductId
        if (shopifyResponse.getProductId() == null) {

            log.warn(
                "SHOPIFY_DELETE_PRODUCT_NOT_FOUND | productCode={} | shopifyProductId={}",
                sync.getProductCode(),
                sync.getShopifyProductId()
            );

            return shopifyResponse;
        }

        log.info(
            "SHOPIFY_DELETE_PRODUCT_SUCCESS | productCode={} | deletedProductId={}",
            sync.getProductCode(),
            shopifyResponse.getProductId()
        );

        return shopifyResponse;

    } catch (Exception e) {

        log.error(
            "SHOPIFY_DELETE_PRODUCT_ERROR | productCode={} | error={}",
            sync != null ? sync.getProductCode() : "UNKNOWN",
            e.getMessage(),
            e
        );

        throw new RuntimeException(
            "Error eliminando producto en Shopify",
            e
        );
    }
    
}
private ShopifyResponse parseDeleteResponse(String response) throws Exception {

    ObjectMapper mapper = new ObjectMapper();

    JsonNode root = mapper.readTree(response);

    JsonNode deleteNode =
        root.path("data")
            .path("productDelete");

    ShopifyResponse result = new ShopifyResponse();

    // 🔥 deletedProductId
    JsonNode deletedId =
        deleteNode.path("deletedProductId");

    if (!deletedId.isMissingNode() && !deletedId.isNull()) {
        result.setProductId(deletedId.asText());
    }

    // 🔥 userErrors
    JsonNode errors =
        deleteNode.path("userErrors");

    if (errors.isArray() && !errors.isEmpty()) {

        List<String> userErrors = new ArrayList<>();

        for (JsonNode error : errors) {

            String field =
                error.path("field").toString();

            String message =
                error.path("message").asText();

            userErrors.add(field + " -> " + message);
        }

        result.setErrors(userErrors);
    }

    return result;
}
}
