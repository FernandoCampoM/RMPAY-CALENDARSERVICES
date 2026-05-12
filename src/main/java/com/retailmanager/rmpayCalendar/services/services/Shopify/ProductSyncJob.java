package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncRepository;
import com.retailmanager.rmpayCalendar.db2.repository.Sys_general_configRepository;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

@Component
public class ProductSyncJob {
    @Autowired
    ProductSyncService productSyncService;
    @Autowired
    ProductSyncRepository productSyncRepository;
    @Autowired
    private  PosClientService posClientService;
     @Autowired
    private Sys_general_configRepository configRepository;

    public void execute() {
        String isFirstRun= configRepository.getIsFirstRun();
        if(isFirstRun == null) {
            configRepository.initializeIsFirstRun();
        }
        List<PosProduct> products = posClientService.getAllProducts();
        
        productSyncService.processProducts(products);

        List<String> productCodes = products.stream().map(PosProduct::getProductCode).toList();
        
        List<ProductSync> deletedProducts = productSyncRepository.findAllByProductCodeNotInAndDeletedFalse(productCodes.subList(0,5));

        productSyncService.processDeletedProducts(deletedProducts);
    }

}