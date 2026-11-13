package com.retailmanager.rmpayCalendar.enums;

/**
 * Sistema que manda ("rey") en el catálogo: precio, descripción y altas de productos.
 * El stock siempre fluye de RM hacia Shopify, sin importar este valor.
 */
public enum SyncMaster {
    RM,
    SHOPIFY,
    NINGUNO
}
