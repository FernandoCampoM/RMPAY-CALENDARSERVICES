package com.retailmanager.rmpayCalendar.services.services.Shopify;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Montos de la factura que se arma desde un pedido de Shopify (mapOrderFromQueryToInvoice).
 * RM API (1.11.0+) exige Total == Subtotal - Descuento + CityTax + StateTax y pagos == Total.
 * No usa Spring ni base de datos: el pedido se arma con la forma que documenta Shopify.
 */
class InvoiceAmountsTest {

    static Stream<Arguments> cases() {
        // nombre, bruto, descuento, state, city, título state, título city, envío, impuesto incluido
        return Stream.of(
                Arguments.of("sin descuento, impuesto ni envío", 50.00, 0.0, 0.0, 0.0, null, null, 0.0, false),
                Arguments.of("descuento 10%", 50.00, 5.00, 0.0, 0.0, null, null, 0.0, false),
                Arguments.of("IVU por nombre", 50.00, 0.0, 5.25, 0.50, "PR State Tax", "Municipal Tax", 0.0, false),
                Arguments.of("IVU por tasa 1%", 50.00, 0.0, 5.25, 0.50, "Tax", "Tax", 0.0, false),
                Arguments.of("envío", 50.00, 0.0, 0.0, 0.0, null, null, 7.99, false),
                Arguments.of("descuento + IVU + envío", 50.00, 5.00, 4.73, 0.45, "PR State Tax", "PR Municipal Tax", 7.99, false),
                Arguments.of("impuesto incluido", 55.75, 0.0, 5.25, 0.50, "State", "City", 0.0, true),
                Arguments.of("céntimos que redondean", 33.33, 3.33, 3.15, 0.30, "State", "Municipal", 4.99, false));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    @SuppressWarnings("unchecked")
    void cumpleLaReglaDeMontosDeRm(String name, double gross, double discount, double state, double city,
            String stateTitle, String cityTitle, double shipping, boolean included) throws Exception {

        double tax = state + city;
        double subtotal = gross - discount;
        double total = subtotal + shipping + (included ? 0 : tax);

        StringBuilder taxLines = new StringBuilder("[");
        if (stateTitle != null) {
            taxLines.append(String.format(Locale.ROOT,
                    "{\"title\":\"%s\",\"rate\":0.105,\"priceSet\":{\"shopMoney\":{\"amount\":\"%.2f\"}}},", stateTitle, state));
            taxLines.append(String.format(Locale.ROOT,
                    "{\"title\":\"%s\",\"rate\":0.01,\"priceSet\":{\"shopMoney\":{\"amount\":\"%.2f\"}}}", cityTitle, city));
        }
        taxLines.append("]");

        String json = String.format(Locale.ROOT,
                "{\"name\":\"#1001\",\"createdAt\":\"2026-11-03T10:00:00Z\",\"taxesIncluded\":%s,"
                        + "\"subtotalPriceSet\":{\"shopMoney\":{\"amount\":\"%.2f\"}},"
                        + "\"totalPriceSet\":{\"shopMoney\":{\"amount\":\"%.2f\"}},"
                        + "\"totalDiscountsSet\":{\"shopMoney\":{\"amount\":\"%.2f\"}},"
                        + "\"totalTaxSet\":{\"shopMoney\":{\"amount\":\"%.2f\"}},\"taxLines\":%s,"
                        + "\"lineItems\":{\"edges\":[{\"node\":{\"name\":\"P\",\"quantity\":1,"
                        + "\"originalUnitPriceSet\":{\"shopMoney\":{\"amount\":\"%.2f\"}},\"variant\":{\"sku\":\"S\"}}}]}}",
                included, subtotal, total, discount, tax, taxLines, gross);

        InvoiceSyncService service = new InvoiceSyncService();
        ReflectionTestUtils.setField(service, "shippingItemCode", "FLETE");
        Map<String, Object> invoice = ReflectionTestUtils.invokeMethod(service, "mapOrderFromQueryToInvoice",
                new ObjectMapper().readTree(json));

        double s = (double) invoice.get("Subtotal");
        double d = (double) invoice.get("Descuento");
        double t = (double) invoice.get("Total");
        double ct = (double) invoice.get("CityTax");
        double st = (double) invoice.get("StateTax");
        double paid = (double) invoice.get("PaidCash") + (double) invoice.get("PaidCreditCard")
                + (double) invoice.get("PaidDebitCard") + (double) invoice.get("PaidCheck")
                + (double) invoice.get("PaidAthMovil");
        List<Map<String, Object>> items = (List<Map<String, Object>>) invoice.get("Items");
        boolean hasFlete = items.stream().anyMatch(i -> "FLETE".equals(i.get("Referencia")));

        assertEquals(t, Math.round((s - d + ct + st) * 100) / 100.0, 0.005, "Total = Subtotal - Descuento + impuestos");
        assertEquals(t, paid, 0.005, "pagos = Total");
        assertEquals(state, st, 0.005, "StateTax");
        assertEquals(city, ct, 0.005, "CityTax");
        assertEquals(shipping > 0, hasFlete, "línea FLETE solo si hay envío");
    }
}
