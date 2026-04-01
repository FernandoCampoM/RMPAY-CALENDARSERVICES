package com.retailmanager.rmpayCalendar.services.services.Shopify;

import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.models.ShopifyResponse;

public interface IShopifyService {
    public ShopifyResponse  createProduct(PosProduct product);

    public void updateProduct(PosProduct product, ProductSync sync);

    public void updateInventory(String inventoryItemId, int quantity);
public ShopifyResponse findBySku(String sku);
}
