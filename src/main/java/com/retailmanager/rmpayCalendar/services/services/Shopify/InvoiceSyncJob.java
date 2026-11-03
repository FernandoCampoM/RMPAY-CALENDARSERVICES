package com.retailmanager.rmpayCalendar.services.services.Shopify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.repository.Sys_general_configRepository;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

@Component
public class InvoiceSyncJob {
     @Autowired
    private InvoiceSyncService invoiceSyncService;
    @Autowired
    private  PosClientService posClientService;
    @Autowired
    private Sys_general_configRepository configRepository;

    public void execute() {
        String lastSyncDate = configRepository.getLastSyncDate();
        String now = nowUTC();
        if(lastSyncDate == null) {
            configRepository.initializeLastSyncDate();
            lastSyncDate = configRepository.getLastSyncDate();
        }

        // 🔁 Primero las facturas que fallaron en ciclos anteriores; un error aquí no frena el ciclo
        try {
            invoiceSyncService.retryFailedOrders();
        } catch (Exception e) {
            System.out.println("Error al reintentar facturas fallidas: " + e.getMessage());
        }

        try {
            invoiceSyncService.syncOrdersToPOS(lastSyncDate);
            // En simulación no se avanza la fecha, para que esos pedidos se lean de nuevo al apagarla
            if (invoiceSyncService.isDryRun()) {
                System.out.println("🧪 SIMULACIÓN activa (shopify.invoice_dry_run): no se envían facturas ni se avanza la fecha de sincronización");
            } else {
                configRepository.updateLastSyncDate(now);
            }
        } catch (Exception e) {
            System.out.println("Error al sincronizar facturas: " + e.getMessage());
        }
        
    }
    public String nowUTC() {
    return java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
            .withNano(0)
            .toString();
}

}
