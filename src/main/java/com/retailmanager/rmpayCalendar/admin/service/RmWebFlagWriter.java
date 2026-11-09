package com.retailmanager.rmpayCalendar.admin.service;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Marca un producto de RM como de comercio electrónico (Inventario.eComerce = 1), directo en
 * la base de RM (db1 = POSDPS). RM API no permite hacerlo: ni ProdCreateNewItem ni
 * ProdMultipleChange aceptan esa marca. Sin ella el producto no sale en GetAllProducts?Web=True
 * y el sync, al no verlo, lo daría por eliminado y lo borraría de Shopify.
 */
@Service
public class RmWebFlagWriter {

    private final JdbcTemplate jdbc;

    public RmWebFlagWriter(@Qualifier("db1DataSource") DataSource db1DataSource) {
        this.jdbc = new JdbcTemplate(db1DataSource);
    }

    public void markAsWeb(String itemCode) {
        int updated = jdbc.update("UPDATE Inventario SET eComerce = 1 WHERE Referencia = ?", itemCode);
        if (updated == 0) {
            throw new IllegalStateException("No se encontró el producto " + itemCode + " en RM para marcarlo como web");
        }
    }
}
