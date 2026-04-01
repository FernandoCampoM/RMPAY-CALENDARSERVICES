package com.retailmanager.rmpayCalendar.models;

import lombok.Data;

@Data
public class ShopifyResponse {

    private String productId;
    private String variantId;
    private String inventoryItemId;
}