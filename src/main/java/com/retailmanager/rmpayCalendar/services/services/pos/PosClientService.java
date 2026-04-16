package com.retailmanager.rmpayCalendar.services.services.pos;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

        String url = baseUrl + "/cse.api.v1/GetAllProducts";

        PosProduct[] response = restTemplate.getForObject(url, PosProduct[].class);

        return Arrays.asList(response);
    }
    public void sendInvoiceToPOS(Map<String, Object> invoice) {
    try {

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(invoice);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/cse.api.v1/ImportExternalInvoice"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        String responseBody = response.body();

        // 🔴 1. Validar HTTP
        if (response.statusCode() != 200) {
            throw new RuntimeException("Error HTTP POS: " + response.statusCode() + " - " + responseBody);
        }

        // 🔥 2. Validar respuesta lógica del POS
        JsonNode jsonResponse = mapper.readTree(responseBody);

        boolean success = jsonResponse.path("success").asBoolean(false);

        if (!success) {
            String message = jsonResponse.path("message").asText("Error desconocido POS");
            throw new RuntimeException("Error lógico POS: " + message);
        }

        System.out.println("✅ Factura enviada al POS");
        System.out.println("Respuesta POS: " + responseBody);

    } catch (Exception e) {
        throw new RuntimeException("Error enviando al POS", e);
    }
}
}