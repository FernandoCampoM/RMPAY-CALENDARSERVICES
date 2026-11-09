package com.retailmanager.rmpayCalendar.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpayCalendar.db2.entity.CatalogMatch;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

/**
 * Alta de productos en Retail Manager desde la conciliación ("Crear en RM").
 * Usa ProdCreateNewItem de RM API (precio de Shopify, costo 0, impuestos por defecto de RM) y
 * luego marca el producto como web en POSDPS (RmWebFlagWriter), porque RM API no permite hacerlo.
 * Si el código ya existe en RM, no se crea otro: solo se marca como web.
 * Límites de RM: código hasta 30 caracteres, descripción hasta 90, código de barras hasta 40.
 */
@Service
public class PosProductWriter {

    private static final int MAX_CODE = 30;
    private static final int MAX_DESCRIPTION = 90;
    private static final int MAX_BARCODE = 40;

    @Autowired
    private PosClientService posClientService;

    @Autowired
    private RmWebFlagWriter rmWebFlagWriter;

    /** El endpoint de RM está fijo en el código: siempre se puede crear. */
    public boolean isConfigured() {
        return true;
    }

    /** @return el código con que quedó el producto en RM */
    public String create(CatalogMatch row, String productCode) throws Exception {
        if (productCode.length() > MAX_CODE) {
            throw new IllegalStateException("El código " + productCode + " tiene más de " + MAX_CODE
                    + " caracteres y RM no lo acepta; acórtelo en Shopify");
        }
        String name = row.getShopifyVariantCount() != null && row.getShopifyVariantCount() > 1
                && row.getShopifyVariantTitle() != null
                ? row.getShopifyTitle() + " " + row.getShopifyVariantTitle()
                : row.getShopifyTitle();
        String description = name == null || name.isBlank() ? productCode : name.trim();
        if (description.length() > MAX_DESCRIPTION) {
            description = description.substring(0, MAX_DESCRIPTION);
        }
        String barcode = row.getShopifyBarcode() == null ? "" : MatchEngine.normalizeCode(row.getShopifyBarcode());
        if (barcode.length() > MAX_BARCODE) {
            barcode = "";
        }

        if (!posClientService.productExists(productCode)) {
            posClientService.createItem(productCode, description, row.getShopifyPrice(), barcode);
        }
        rmWebFlagWriter.markAsWeb(productCode);
        return productCode;
    }
}
