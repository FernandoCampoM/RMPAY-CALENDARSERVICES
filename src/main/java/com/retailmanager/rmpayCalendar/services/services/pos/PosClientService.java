package com.retailmanager.rmpayCalendar.services.services.pos;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PosClientService {

    private final RestTemplate restTemplate;

    @Value("${pos.base-url}")
    private String baseUrl;

    public List<PosProduct> getAllProducts() {

        String url = baseUrl + "/cse.api.v1/GetAllProducts?Web=True";

        PosProduct[] response = restTemplate.getForObject(url, PosProduct[].class);

        return Arrays.asList(response);
    }
    public void sendInvoiceToPOS(Map<String, Object> invoice) {
    try {

        ObjectMapper mapper = new ObjectMapper();

        // 🔥 1. Convertir a JSON string
        String invoiceJson = mapper.writeValueAsString(invoice);

        // 🔥 2. Formato form-urlencoded
        String formData = "InvoiceData=" + URLEncoder.encode(invoiceJson, StandardCharsets.UTF_8);
        System.out.println("📦 Datos enviados al POS: " + formData);
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/cse.api.v1/ImportExternalInvoice"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        String responseBody = response.body();

        System.out.println("📦 RESPUESTA POS: " + responseBody);

        // 🔥 3. Parsear SIEMPRE (aunque HTTP sea 200)
        JsonNode jsonResponse = mapper.readTree(responseBody);

        boolean success = jsonResponse.path("success").asBoolean(false);
        int status = jsonResponse.path("status").asInt(0);
        String message = jsonResponse.path("message").asText("Sin mensaje");

        // 🔴 4. Validación REAL
        if (!success) {
            throw new RuntimeException("❌ POS rechazó la factura | status=" + status + " | message=" + message);
        }

        // 🔥 5. Validación adicional (por si acaso)
        if (status != 200) {
            throw new RuntimeException("⚠️ POS respondió success=true pero status != 200 | status=" + status+" | message=" + message);
        }

        // ✅ OK
        System.out.println("✅ Factura enviada correctamente al POS");
        System.out.println("🧾 InvoiceNumber: " + jsonResponse.path("InvoiceNumber").asText());
        System.out.println("📦 ItemsImported: " + jsonResponse.path("ItemsImported").asInt());

    } catch (Exception e) {
        throw new RuntimeException("Error enviando al POS"+ e.getMessage(), e);
    }
}
}