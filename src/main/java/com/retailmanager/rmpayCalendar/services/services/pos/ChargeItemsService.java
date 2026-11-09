package com.retailmanager.rmpayCalendar.services.services.pos;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Productos de RM con los que se facturan los cargos que no son productos de Shopify:
 * el envío (shopify.shipping_item_code) y la propina (shopify.tip_item_code).
 * Si no existen en RM se crean solos (precio 0, exentos de impuestos: el IVU ya viene en la
 * factura), para no depender de que alguien los cree a mano. En simulación solo se avisa.
 * Una vez confirmado un producto no se vuelve a consultar.
 * Depende de PosClientService.productExists y PosClientService.createProduct.
 */
@Service
public class ChargeItemsService {

    @Autowired
    private PosClientService posClientService;

    @Value("${shopify.shipping_item_code}")
    private String shippingItemCode;

    @Value("${shopify.tip_item_code}")
    private String tipItemCode;

    @Value("${shopify.invoice_dry_run}")
    private boolean invoiceDryRun;

    private final Set<String> confirmed = ConcurrentHashMap.newKeySet();

    /** Revisa los dos productos y crea los que falten. Nunca lanza excepción: los errores van al log. */
    public void ensureChargeItems() {
        ensure("envío", shippingItemCode, "ENVIO SHOPIFY", "shopify.shipping_item_code");
        ensure("propina", tipItemCode, "PROPINA SHOPIFY", "shopify.tip_item_code");
    }

    private void ensure(String label, String itemCode, String description, String property) {
        if (itemCode == null || itemCode.isBlank() || confirmed.contains(itemCode)) {
            return;
        }
        try {
            if (posClientService.productExists(itemCode)) {
                confirmed.add(itemCode);
                System.out.println("✅ Producto de " + label + " encontrado en RM: " + itemCode);
                return;
            }
            if (invoiceDryRun) {
                System.out.println("🧪 SIMULACIÓN | El producto de " + label + " '" + itemCode
                        + "' no existe en RM; fuera de simulación se crearía automáticamente");
                return;
            }
            posClientService.createProduct(itemCode, description, 0, 0, 0);
            confirmed.add(itemCode);
            System.out.println("✅ Producto de " + label + " creado en RM: " + itemCode + " (" + description + ")");
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo revisar o crear el producto de " + label + " '" + itemCode
                    + "' en RM (" + property + "): " + e.getMessage());
        }
    }
}
