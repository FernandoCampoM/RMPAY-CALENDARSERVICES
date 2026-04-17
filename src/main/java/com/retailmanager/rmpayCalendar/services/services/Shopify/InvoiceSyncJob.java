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

        try {
            invoiceSyncService.syncOrdersToPOS(lastSyncDate);
            configRepository.updateLastSyncDate(now);
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
