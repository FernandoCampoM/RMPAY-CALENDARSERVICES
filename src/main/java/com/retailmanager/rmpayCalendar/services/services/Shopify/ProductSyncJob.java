package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

@Component
public class ProductSyncJob {
    @Autowired
    ProductSyncService productSyncService;
    @Autowired
    private  PosClientService posClientService;

    public void execute() {

        List<PosProduct> products = posClientService.getAllProducts();

        productSyncService.processProducts(products);
    }
}