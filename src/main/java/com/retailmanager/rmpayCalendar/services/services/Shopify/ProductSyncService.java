package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpayCalendar.admin.service.ShopifyCatalogClient;
import com.retailmanager.rmpayCalendar.admin.service.SyncSettingsService;
import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSyncEvent;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncRepository;
import com.retailmanager.rmpayCalendar.db2.repository.Sys_general_configRepository;
import com.retailmanager.rmpayCalendar.enums.SyncAction;
import com.retailmanager.rmpayCalendar.enums.SyncMaster;
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
    // NUEVO: modo rey y bloqueo de altas (panel admin)
    @Autowired
    private SyncSettingsService syncSettings;
    // NUEVO: búsqueda de variantes por código de barras
    @Autowired
    private ShopifyCatalogClient catalogClient;

    /**
     * MODIFICADO (aprobado): respeta el modo rey (sync.master) y el bloqueo de altas (sync.createEnabled).
     *  - Producto sin vínculo: solo se crea/vincula si RM es el rey y la conciliación está cerrada.
     *    Siempre se busca antes en Shopify por SKU y por código de barras (ya no se omite en la primera ejecución).
     *  - Producto vinculado con cambios: si RM es el rey se actualiza todo; si no, solo el stock.
     *  - Un error en un producto se registra y no detiene el resto.
     */
    public void processProducts(List<PosProduct> products) {
        String isFirstRunS = configRepository.getIsFirstRun();
        Boolean isFirstRun = isFirstRunS == null ? false : (isFirstRunS.equals("true") ? true : false);

        LocalDateTime now = LocalDateTime.now();
        SyncMaster master = syncSettings.getMaster();
        boolean createEnabled = syncSettings.isCreateEnabled();

        for (PosProduct product : products) {
          try {

            String hash = HashUtils.generateProductHash(product);

            ProductSync existing = repository.findByProductCode(product.getProductCode());

            if (existing == null) {
                if (master != SyncMaster.RM || !createEnabled) {
                    // Queda para la pantalla de conciliación
                    log.debug("SYNC_SKIP_CREATE | code={} | master={} | createEnabled={}",
                            product.getProductCode(), master, createEnabled);
                    continue;
                }
                {
                    // 🔥 CONSULTA FALLBACK EN SHOPIFY (por SKU y luego por código de barras)
                    ShopifyResponse found = shopifyService.findBySku(product.getProductCode());
                    if (found == null) {
                        found = catalogClient.findByBarcode(product.getBarCode());
                    }
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
                }

            } else if (!hash.equals(existing.getHash())) {

                existing.setLastSeen(now);
                repository.save(existing);

                if (master != SyncMaster.RM) {
                    // Shopify o nadie es el rey: precio y descripción no se tocan, el stock siempre va RM → Shopify
                    shopifyService.updateInventory(existing.getShopifyInventoryItemId(), product.getCurrentStock());
                    existing.setHash(hash);
                    existing.setLastSync(LocalDateTime.now());
                    repository.save(existing);
                    continue;
                }
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
          } catch (Exception e) {
            log.error("SYNC_PRODUCT_ERROR | code={} | error={}", product.getProductCode(), e.getMessage());
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
