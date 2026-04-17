package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShopifyGraphQLClient {

    private final RestTemplate restTemplate;

    @Value("${shopify.url}")
    private String url;
    @Value("${shopify.urlAccesToken}")
    private String urlAccesToken;
    @Value("${shopify.client_secret}")
    private String client_secret;
    @Value("${shopify.client_id}")
    private String client_id;
    @Value("${shopify.grant_type}")
    private String grant_type;

    private String token;

    public ShopifyGraphQLClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @PostConstruct
    public void init() {
        try {
            this.token = getShopifyAccessToken();
            System.out.println("✅ Token inicializado");
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo inicializar el token al arranque");
        }
    }

    @SuppressWarnings("null")
    public String execute(String query) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Shopify-Access-Token", token);

        Map<String, String> body = Map.of("query", query);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        String responseBody = response.getBody();
        log.info("Shopify response: {}", responseBody);
        // 🔥 Validar error dentro del body (Shopify lo manda así)
        if (responseBody != null && responseBody.contains("Invalid API key or access token")) {
            this.token = getShopifyAccessToken();
            return execute(query);
        }

        return response.getBody();
    }

    @SuppressWarnings("null")
    public String execute(String query, Map<String, Object> variables) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Shopify-Access-Token", token);

        Map<String, Object> body = Map.of(
                "query", query,
                "variables", variables);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        String responseBody = response.getBody();
        log.info("Shopify response: {}", responseBody);
        // 🔥 Validar error dentro del body (Shopify lo manda así)
        if (responseBody != null && responseBody.contains("Invalid API key or access token")) {
            this.token = getShopifyAccessToken();
            return execute(query, variables);
        }

        return response.getBody();
    }

    public String getShopifyAccessToken() {
        try {

            HttpClient client = HttpClient.newHttpClient();

            // 🔥 Body tipo x-www-form-urlencoded
            String formData = "grant_type=" + URLEncoder.encode(grant_type, StandardCharsets.UTF_8)
                    + "&client_id=" + URLEncoder.encode(client_id, StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(client_secret, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlAccesToken))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            // 🔴 Validar HTTP
            if (response.statusCode() != 200) {
                throw new RuntimeException("Error obteniendo token Shopify: "
                        + response.statusCode() + " - " + response.body());
            }

            // 🔥 Parsear respuesta
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());

            String accessToken = json.path("access_token").asText();

            if (accessToken == null || accessToken.isEmpty()) {
                throw new RuntimeException("Token no recibido de Shopify: " + response.body());
            }

            System.out.println("✅ Token obtenido correctamente");

            return accessToken;

        } catch (Exception e) {
            throw new RuntimeException("Error obteniendo token Shopify", e);
        }
    }
}