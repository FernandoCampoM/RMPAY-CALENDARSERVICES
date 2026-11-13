package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSyncEvent;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncRepository;
import com.retailmanager.rmpayCalendar.enums.SyncAction;
import com.retailmanager.rmpayCalendar.models.ShopifyResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductSyncConsumer {

    private final IShopifyService shopifyService;
    private final ProductSyncRepository repository;

                        // TODO:DESCOMENTAR SI SE USAN COLAS
    //@RabbitListener(queues = "shopify.product.queue") 
    public void process(ProductSyncEvent event) {

        PosProduct product = event.getProduct();

        log.info("SYNC_PRODUCT | code={} | action={} | status=START",
                product.getProductCode(), event.getAction());

        try {

            if (event.getAction() == SyncAction.CREATE) {

                // 🔥 1. CREAR PRODUCTO EN SHOPIFY
                ShopifyResponse response = shopifyService.createProduct(product);

                // 🔥 2. GUARDAR EN DB
                ProductSync sync = new ProductSync();
                sync.setProductCode(product.getProductCode());
                sync.setShopifyProductId(response.getProductId());
                sync.setShopifyVariantId(response.getVariantId());
                sync.setShopifyInventoryItemId(response.getInventoryItemId());
                sync.setHash(event.getHash());
                sync.setLastSync(LocalDateTime.now());
                sync.setLastSeen(LocalDateTime.now());

                repository.save(sync);

            } else if (event.getAction() == SyncAction.UPDATE) {

                // 🔥 1. BUSCAR EXISTENTE
                ProductSync sync = repository.findByProductCode(product.getProductCode());

                if (sync == null) {
                    log.error("SYNC_ERROR | code={} | error=NOT_FOUND_IN_DB",
                            product.getProductCode());
                    return;
                }

                // 🔥 2. ACTUALIZAR PRODUCTO
                shopifyService.updateProduct(product, sync);

                // 🔥 3. ACTUALIZAR INVENTARIO
                shopifyService.updateInventory(
                        sync.getShopifyInventoryItemId(),
                        product.getCurrentStock()
                );

                // 🔥 4. ACTUALIZAR HASH + FECHAS
                sync.setHash(event.getHash());
                sync.setLastSync(LocalDateTime.now());
                sync.setLastSeen(LocalDateTime.now());

                repository.save(sync);
            }

            log.info("SYNC_PRODUCT | code={} | status=SUCCESS",
                    product.getProductCode());

        } catch (Exception e) {

            log.error("SYNC_ERROR | code={} | error={}",
                    product.getProductCode(), e.getMessage());

            // 🔥 IMPORTANTE: lanzar excepción para que Rabbit reintente
            throw new RuntimeException(e);
        }
    }
}
