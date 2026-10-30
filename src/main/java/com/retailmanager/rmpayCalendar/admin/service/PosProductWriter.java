package com.retailmanager.rmpayCalendar.admin.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpayCalendar.db2.entity.CatalogMatch;

/**
 * Alta de productos en Retail Manager a través del POS.
 * El POS todavía no expone este endpoint: la ruta se configura en el panel (pos.createProductPath).
 * Mientras esté vacía, las filas quedan en CREAR_EN_RM esperando.
 *
 * Contrato propuesto al equipo del POS (JSON, mismo estilo de respuesta que ImportExternalInvoice):
 *   POST {pos.base-url}{pos.createProductPath}
 *   { "ProductCode", "ProductName", "Price", "BarCode", "WebDesc", "Web": true }
 *   → { "success": true, "status": 200, "message": "...", "ProductCode": "..." }
 */
@Service
public class PosProductWriter {

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${pos.base-url}")
    private String baseUrl;

    @Autowired
    private SyncSettingsService settings;

    public boolean isConfigured() {
        return !settings.getPosCreateProductPath().isBlank();
    }

    /** @return el código con que quedó el producto en RM */
    public String create(CatalogMatch row, String productCode) throws Exception {
        String path = settings.getPosCreateProductPath();
        if (path.isBlank()) {
            throw new IllegalStateException("El endpoint del POS para crear productos no está configurado");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ProductCode", productCode);
        body.put("ProductName", row.getShopifyVariantCount() != null && row.getShopifyVariantCount() > 1
                && row.getShopifyVariantTitle() != null
                ? row.getShopifyTitle() + " " + row.getShopifyVariantTitle()
                : row.getShopifyTitle());
        body.put("Price", row.getShopifyPrice());
        body.put("BarCode", row.getShopifyBarcode() == null ? "" : MatchEngine.normalizeCode(row.getShopifyBarcode()));
        body.put("WebDesc", row.getShopifyTitle());
        body.put("Web", true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode json = mapper.readTree(response.body());
        if (response.statusCode() != 200 || !json.path("success").asBoolean(false)) {
            throw new IllegalStateException("POS rechazó el producto: " + json.path("message").asText(response.body()));
        }
        String created = json.path("ProductCode").asText("");
        return created.isEmpty() ? productCode : created;
    }
}
