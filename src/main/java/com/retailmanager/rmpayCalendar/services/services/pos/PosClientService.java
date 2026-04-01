package com.retailmanager.rmpayCalendar.services.services.pos;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
}