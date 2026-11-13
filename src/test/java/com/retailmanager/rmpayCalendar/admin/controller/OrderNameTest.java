package com.retailmanager.rmpayCalendar.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Número del pedido de Shopify que muestra el panel en Pedidos, sacado del JSON guardado. */
class OrderNameTest {

    @Test
    void leeElNombreDelPedidoDelCicloYDelWebhook() {
        assertEquals("#1006", AdminApiController.orderName("{\"id\":\"gid://shopify/Order/1\",\"name\":\"#1006\"}"));
        assertEquals("#1077", AdminApiController.orderName("{\"admin_graphql_api_id\":\"x\",\"name\":\"#1077\"}"));
    }

    @Test
    void sinNombreOJsonInvalidoNoFalla() {
        assertNull(AdminApiController.orderName(null));
        assertNull(AdminApiController.orderName("{}"));
        assertNull(AdminApiController.orderName("no es json"));
    }
}
