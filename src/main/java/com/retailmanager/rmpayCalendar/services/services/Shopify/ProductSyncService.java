package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncRepository;
import com.retailmanager.rmpayCalendar.db2.repository.Sys_general_configRepository;
import com.retailmanager.rmpayCalendar.enums.SyncAction;
import com.retailmanager.rmpayCalendar.models.ShopifyResponse;
import com.retailmanager.rmpayCalendar.utils.HashUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductSyncService {
    @Autowired
    private ProductSyncRepository repository;
    @Autowired
    private IShopifyService shopifyService;
    @Autowired
    private ProductSyncProducer producer;
    @Autowired
    private Sys_general_configRepository configRepository;

    public void processProducts(List<PosProduct> products) {
        String isFirstRunS = configRepository.getIsFirstRun();
        Boolean isFirstRun = isFirstRunS == null ? false : (isFirstRunS.equals("true") ? true : false);

        LocalDateTime now = LocalDateTime.now();

        for (PosProduct product : products) {

            String hash = HashUtils.generateProductHash(product);

            ProductSync existing = repository.findByProductCode(product.getProductCode());

            if (existing == null) {
                if (!isFirstRun) {
                    // 🔥 CONSULTA FALLBACK EN SHOPIFY
                    ShopifyResponse found = shopifyService.findBySku(product.getProductCode());

                    if (found != null) {

                        log.info("PRODUCT_ALREADY_EXISTS_IN_SHOPIFY | code={}", product.getProductCode());

                        // 🔥 GUARDAR EN DB (RECUPERACIÓN)
                        ProductSync sync = new ProductSync();
                        sync.setProductCode(product.getProductCode());
                        sync.setShopifyProductId(found.getProductId());
                        sync.setShopifyVariantId(found.getVariantId());
                        sync.setShopifyInventoryItemId(found.getInventoryItemId());
                        sync.setHash(hash);
                        sync.setLastSync(LocalDateTime.now());
                        sync.setLastSeen(LocalDateTime.now());

                        repository.save(sync);

                    } else {

                        // 🔥 CREAR NORMAL
                        producer.send(product, SyncAction.CREATE, hash);
                    }
                } else {

                    // 🔥 CREAR NORMAL
                    producer.send(product, SyncAction.CREATE, hash);
                }

            } else if (!existing.getHash().equals(hash)) {

                existing.setLastSeen(now);
                repository.save(existing);

                producer.send(product, SyncAction.UPDATE, hash);
            }
        }
        if(isFirstRun) {
            configRepository.updateIsFirstRun("false");
        }
    }

    public void processProduct(PosProduct product) {
        
        String hash = HashUtils.generateProductHash(product);

        ProductSync existing = repository.findByProductCode(product.getProductCode());

        if (existing == null) {

            // 🔥 CONSULTA FALLBACK EN SHOPIFY
            ShopifyResponse found = shopifyService.findBySku(product.getProductCode());

            if (found != null) {

                log.info("PRODUCT_ALREADY_EXISTS_IN_SHOPIFY | code={}", product.getProductCode());

                // 🔥 GUARDAR EN DB (RECUPERACIÓN)
                ProductSync sync = new ProductSync();
                sync.setProductCode(product.getProductCode());
                sync.setShopifyProductId(found.getProductId());
                sync.setShopifyVariantId(found.getVariantId());
                sync.setShopifyInventoryItemId(found.getInventoryItemId());
                sync.setHash(hash);
                sync.setLastSync(LocalDateTime.now());
                sync.setLastSeen(LocalDateTime.now());

                repository.save(sync);

            } else {

                // 🔥 CREAR NORMAL
                producer.send(product, SyncAction.CREATE, hash);
            }

        } else {

            existing.setLastSeen(LocalDateTime.now());

            if (!existing.getHash().equals(hash)) {
                producer.send(product, SyncAction.UPDATE, hash);
            }

            repository.save(existing);
        }
    }

    public void processDeletedProducts() {

        LocalDateTime limit = LocalDateTime.now().minusHours(1);

        List<ProductSync> oldProducts = repository.findAllByLastSeenBefore(limit);

        for (ProductSync product : oldProducts) {
            log.info("PRODUCT_NOT_IN_POS | code={}", product.getProductCode());
            // TODO
            // aquí puedes:
            // 1. poner stock en 0
            // 2. o desactivar producto en Shopify
        }
    }

}
