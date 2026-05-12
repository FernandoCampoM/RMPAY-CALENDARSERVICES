package com.retailmanager.rmpayCalendar.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ShopifyResponse {

    private String productId;
    private String variantId;
    private String inventoryItemId;

    private List<String> errors = new ArrayList<>();





public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
}
}