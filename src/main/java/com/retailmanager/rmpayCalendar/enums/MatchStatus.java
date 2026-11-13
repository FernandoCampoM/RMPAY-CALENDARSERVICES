package com.retailmanager.rmpayCalendar.enums;

/**
 * Estado de una fila de conciliación Shopify ↔ Retail Manager.
 */
public enum MatchStatus {
    /** Coincide por código (SKU / código de barras) con un único producto de RM. */
    AUTO,
    /** Parecido por nombre; requiere revisión humana. */
    SUGERIDO,
    /** El código coincide con más de un producto de RM. */
    AMBIGUO,
    /** Variante de Shopify sin contraparte en RM. */
    SIN_MATCH,
    /** Producto de RM (Web=True) sin contraparte en Shopify. */
    SOLO_RM,
    /** Ya está relacionado en ProductSync. */
    VINCULADO,
    /** Descartado por el usuario; se conserva entre análisis. */
    IGNORADO,
    /** Marcado para crearse en RM; espera el endpoint del POS. */
    CREAR_EN_RM,
    /** La última acción falló; ver nota. */
    ERROR
}
