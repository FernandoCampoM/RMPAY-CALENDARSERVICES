package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.util.List;

import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.models.ShopifyResponse;
import com.retailmanager.rmpayCalendar.services.DTO.ShopifyPublication;

public interface IShopifyService {
    public ShopifyResponse  createProduct(PosProduct product);

    public void updateProduct(PosProduct product, ProductSync sync);

    public void updateInventory(String inventoryItemId, int quantity);
    public void publishProductInAllChannels(String productId);
    public List<String> getPublicationIdsByProduct(String productId);
    public List<ShopifyPublication> getAvailablePublications();
    public void publishProduct(String productId, String publicationId) ;
    public String getRecentOrders();
public ShopifyResponse findBySku(String sku);
}
