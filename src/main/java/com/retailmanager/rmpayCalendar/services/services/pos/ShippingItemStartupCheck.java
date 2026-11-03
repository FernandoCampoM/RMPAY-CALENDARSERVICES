package com.retailmanager.rmpayCalendar.services.services.pos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Al arrancar, revisa que existan en RM los productos con los que se facturan el envío
 * (shopify.shipping_item_code) y la propina (shopify.tip_item_code).
 * Solo avisa en el log: no detiene el servicio.
 * Depende de PosClientService.productExists (ProductInfo de RM API).
 */
@Component
public class ShippingItemStartupCheck {

    @Autowired
    private PosClientService posClientService;

    @Value("${shopify.shipping_item_code}")
    private String shippingItemCode;

    @Value("${shopify.tip_item_code}")
    private String tipItemCode;

    @EventListener(ApplicationReadyEvent.class)
    public void checkShippingItem() {
        check("envío", shippingItemCode, "shopify.shipping_item_code");
        check("propina", tipItemCode, "shopify.tip_item_code");
    }

    private void check(String label, String itemCode, String property) {
        try {
            if (posClientService.productExists(itemCode)) {
                System.out.println("✅ Producto de " + label + " encontrado en RM: " + itemCode);
            } else {
                System.out.println("⚠️ El producto de " + label + " '" + itemCode
                        + "' no existe en RM: las facturas con " + label + " pueden ser rechazadas."
                        + " Créelo en RM o cambie " + property);
            }
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo revisar el producto de " + label + " '" + itemCode
                    + "' en RM: " + e.getMessage());
        }
    }
}
