package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.retailmanager.rmpayCalendar.admin.service.AuditService;
import com.retailmanager.rmpayCalendar.admin.service.SyncSettingsService;
import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncAdminRepository;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncRepository;
import com.retailmanager.rmpayCalendar.enums.SyncMaster;
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
    // NUEVO: modo rey, límite de borrado y auditoría (panel admin)
    @Autowired
    private SyncSettingsService syncSettings;
    @Autowired
    private ProductSyncAdminRepository productSyncAdminRepository;
    @Autowired
    private AuditService auditService;

    public void execute() {
        String isFirstRun= configRepository.getIsFirstRun();
        if(isFirstRun == null) {
            configRepository.initializeIsFirstRun();
        }
        List<PosProduct> products = posClientService.getAllProducts();
        
        productSyncService.processProducts(products);

        // MODIFICADO (aprobado): solo se borra en Shopify si RM es el rey, nunca con la lista vacía
        // y nunca más del porcentaje configurado (sync.maxDeletePercent) de los productos vinculados.
        if (syncSettings.getMaster() != SyncMaster.RM) {
            return;
        }
        if (products.isEmpty()) {
            System.out.println("⚠️ El POS devolvió 0 productos: se omite el borrado en Shopify");
            return;
        }

        List<String> productCodes = products.stream().map(PosProduct::getProductCode).toList();

        List<ProductSync> deletedProducts = productSyncRepository.findAllByProductCodeNotInAndDeletedFalse(productCodes);

        long linked = productSyncAdminRepository.countByDeletedFalse();
        int maxPercent = syncSettings.getMaxDeletePercent();
        if (!deletedProducts.isEmpty() && deletedProducts.size() * 100L > linked * maxPercent) {
            String detail = deletedProducts.size() + " de " + linked + " productos vinculados (límite " + maxPercent + "%)";
            System.out.println("⚠️ Borrado masivo bloqueado: " + detail);
            auditService.log(null, "BORRADO_BLOQUEADO", detail);
            return;
        }

        productSyncService.processDeletedProducts(deletedProducts);
    }

}