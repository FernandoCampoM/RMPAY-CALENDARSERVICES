package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSyncEvent;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncRepository;
import com.retailmanager.rmpayCalendar.db2.repository.Sys_general_configRepository;
import com.retailmanager.rmpayCalendar.enums.SyncAction;
import com.retailmanager.rmpayCalendar.models.ShopifyResponse;
import com.retailmanager.rmpayCalendar.services.DTO.ShopifyPublication;
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
                    //TODO: Revisar como proceder cuando el producto ya existe pero 
                    // el inventario de shopify es diferen a la de RM
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
                        shopifyService.updateInventory(found.getInventoryItemId(), product.getCurrentStock());
                        List<String> channels = shopifyService.getPublicationIdsByProduct(found.getProductId());
                        List<ShopifyPublication> publications = shopifyService.getAvailablePublications();
                        for (ShopifyPublication publication : publications) {
                            if (!channels.contains(publication.getId())) {
                                shopifyService.publishProduct(found.getProductId(), publication.getId());
                            }
                        }

                    } else {

                        // 🔥 CREAR NORMAL TODO:DESCOMENTAR SI SE USAN COLAS
                        // producer.send(product, SyncAction.CREATE, hash);

                        //TODO:comentar si se usa colas
                        ProductSyncEvent event = new ProductSyncEvent();
                        event.setProduct(product);
                        event.setAction(SyncAction.CREATE);
                        event.setHash(hash);
                        process(event);
                    }
                } else {

                    // 🔥 CREAR NORMAL TODO:DESCOMENTAR SI SE USAN COLAS
                    //producer.send(product, SyncAction.CREATE, hash);

                    //TODO:comentar si se usa colas
                        ProductSyncEvent event = new ProductSyncEvent();
                        event.setProduct(product);
                        event.setAction(SyncAction.CREATE);
                        event.setHash(hash);
                        process(event);


                }

            } else if (!existing.getHash().equals(hash)) {

                existing.setLastSeen(now);
                repository.save(existing);
                //TODO:DESCOMENTAR SI SE USAN COLAS
                //producer.send(product, SyncAction.UPDATE, hash);
                //TODO:comentar si se usa colas
                        ProductSyncEvent event = new ProductSyncEvent();
                        event.setProduct(product);
                        event.setAction(SyncAction.UPDATE);
                        event.setHash(hash);
                        process(event);
                        List<String> channels = shopifyService.getPublicationIdsByProduct(existing.getShopifyProductId());
                        List<ShopifyPublication> publications = shopifyService.getAvailablePublications();
                        for (ShopifyPublication publication : publications) {
                            if (!channels.contains(publication.getId())) {
                                shopifyService.publishProduct(existing.getShopifyProductId(), publication.getId());
                            }
                        }
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

    public void processDeletedProducts(List<ProductSync> oldProducts) {

      

        for (ProductSync product : oldProducts) {

            // 🔥 ELIMINAR PRODUCTO DE SHOPIFY
            ShopifyResponse response = shopifyService.deleteProduct(product);
            if(response != null && response.getProductId() != null) {
               
               product.setDeleted(true);
               repository.save(product);
               log.info("PRODUCT_DELETED_IN_SHOPIFY | code={}", product.getProductCode());
            }
        }
    }

    //METODOS TEMPORALES SINO SE USA RABBIT
    public void process(ProductSyncEvent event) {

        PosProduct product = event.getProduct();

        log.info("SYNC_PRODUCT | code={} | action={} | status=START",
                product.getProductCode(), event.getAction());

        try {

            if (event.getAction() == SyncAction.CREATE) {

                // 🔥 1. CREAR PRODUCTO EN SHOPIFY
                ShopifyResponse response = shopifyService.createProduct(product);
                System.out.println("-----------------ACTUALIZAR INVENTARIO------------------");
                System.out.println("response: "+response.toString());
                shopifyService.updateInventory(response.getInventoryItemId(), product.getCurrentStock());
                shopifyService.publishProductInAllChannels(response.getProductId());

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
