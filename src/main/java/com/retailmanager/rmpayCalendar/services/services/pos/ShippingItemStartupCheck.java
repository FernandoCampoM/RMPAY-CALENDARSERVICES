package com.retailmanager.rmpayCalendar.services.services.pos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Al arrancar, asegura que existan en RM los productos con los que se facturan el envío
 * (shopify.shipping_item_code) y la propina (shopify.tip_item_code); si faltan, se crean.
 * Si RM no responde, se reintenta en cada ciclo de facturas (InvoiceSyncJob).
 * No detiene el servicio. Depende de ChargeItemsService.
 */
@Component
public class ShippingItemStartupCheck {

    @Autowired
    private ChargeItemsService chargeItemsService;

    @EventListener(ApplicationReadyEvent.class)
    public void checkShippingItem() {
        chargeItemsService.ensureChargeItems();
    }
}
