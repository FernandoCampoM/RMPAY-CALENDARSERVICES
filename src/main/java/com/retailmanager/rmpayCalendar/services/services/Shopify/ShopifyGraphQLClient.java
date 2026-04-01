package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ShopifyGraphQLClient {

    private final RestTemplate restTemplate;

    @Value("${shopify.url}")
    private String url;

    @Value("${shopify.token}")
    private String token;

    public ShopifyGraphQLClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @SuppressWarnings("null")
    public String execute(String query) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Shopify-Access-Token", token);

        Map<String, String> body = Map.of("query", query);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        log.info("Shopify response: {}", response.getBody());

        return response.getBody();
    }
}
