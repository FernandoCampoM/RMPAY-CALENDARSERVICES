package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Pipe.SourceChannel;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpayCalendar.db2.entity.FailedOrder;
import com.retailmanager.rmpayCalendar.db2.entity.ProcessedOrder;
import com.retailmanager.rmpayCalendar.db2.repository.FailedOrderRepository;
import com.retailmanager.rmpayCalendar.db2.repository.ProcessedOrderRepository;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncRepository;
import com.retailmanager.rmpayCalendar.db2.repository.Sys_general_configRepository;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;
import com.retailmanager.rmpayCalendar.services.services.webhook.WebhookSettingsService;
import com.retailmanager.rmpayCalendar.services.services.webhook.WebhookTrace;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InvoiceSyncService {
    /** Tope de seguridad por ciclo: 100 páginas x 50 = 5000 órdenes. */
    private static final int MAX_ORDER_PAGES = 100;
    @Autowired
    private ProcessedOrderRepository repository;
    @Autowired
    private IShopifyService shopifyService;
    @Autowired
    private ProductSyncProducer producer;
    @Autowired
    private Sys_general_configRepository configRepository;
    @Autowired
    private PosClientService posClientService;
    @Value("${shopify.webhook_secret}")
    private String webhookSecret;
    /** Llave de firma configurada en el panel (si no hay, se usa webhookSecret). */
    @Autowired
    private WebhookSettingsService webhookSettingsService;
    /** Producto de RM con el que se factura el envío (debe existir en Inventario). */
    @Value("${shopify.shipping_item_code}")
    private String shippingItemCode;
    /** Producto de RM con el que se factura la propina (debe existir en Inventario). */
    @Value("${shopify.tip_item_code}")
    private String tipItemCode;
    /** Simulación: arma las facturas y las escribe en el log, pero no las envía a RM. */
    @Value("${shopify.invoice_dry_run}")
    private boolean invoiceDryRun;
    @Autowired
    private FailedOrderRepository failedOrderRepository;

    /** Intentos de envío a RM antes de dejar la factura para revisión manual en el panel. */
    public static final int MAX_FAILED_ATTEMPTS = 10;

    public boolean isDryRun() {
        return invoiceDryRun;
    }

    /** Monto de un campo MoneyBag de Shopify ({shopMoney{amount}}), a 2 decimales. */
    private BigDecimal money(JsonNode moneyBag) {
        return decimal(moneyBag.path("shopMoney").path("amount"));
    }

    /** Monto en texto ("12.50") de la API REST/webhook de Shopify, a 2 decimales; vacío o null = 0. */
    private BigDecimal decimal(JsonNode amount) {
        String text = amount.asText("0").trim();
        return new BigDecimal(text.isEmpty() ? "0" : text).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Parte municipal del IVU. Se reconoce por el nombre del impuesto (municipal/city);
     * si el nombre no lo dice, por la tasa del 1% que usa Puerto Rico.
     */
    private BigDecimal cityTaxOf(JsonNode taxLines) {
        BigDecimal city = BigDecimal.ZERO;
        for (JsonNode line : taxLines) {
            if (isCityTaxLine(line)) {
                city = city.add(money(line.path("priceSet")));
            }
        }
        return city;
    }

    /** Igual que cityTaxOf, para tax_lines del webhook (el monto viene en "price"). */
    private BigDecimal cityTaxOfWebhook(JsonNode taxLines) {
        BigDecimal city = BigDecimal.ZERO;
        for (JsonNode line : taxLines) {
            if (isCityTaxLine(line)) {
                city = city.add(decimal(line.path("price")));
            }
        }
        return city;
    }

    private boolean isCityTaxLine(JsonNode line) {
        String title = line.path("title").asText("").toLowerCase();
        double rate = line.path("rate").asDouble(0);
        boolean byTitle = title.contains("munic") || title.contains("city");
        boolean byRate = Math.abs(rate - 0.01) < 0.0001;
        return byTitle || (byRate && !title.contains("state") && !title.contains("estatal"));
    }

    /**
     * 💰 MONTOS: RM API (1.11.0+) rechaza la factura si
     * Total != Subtotal - Descuento + CityTax + StateTax.
     * En Shopify el subtotal ya trae los descuentos restados y el total incluye impuestos,
     * envío y propina, así que el Subtotal se arma con las líneas a precio original (grossLines)
     * más una línea de envío (shippingItemCode) y otra de propina (tipItemCode) si los hay.
     * El envío es lo que queda del total; si hubiera aranceles, también quedan ahí.
     */
    private void applyAmounts(Map<String, Object> invoice, List<Map<String, Object>> items, String orderName,
            BigDecimal grossLines, BigDecimal shopifySubtotal, BigDecimal shopifyTotal, BigDecimal tax,
            boolean taxesIncluded, BigDecimal cityTax, BigDecimal tip) {

        BigDecimal stateTax = tax.subtract(cityTax);
        BigDecimal gross = grossLines.setScale(2, RoundingMode.HALF_UP);
        BigDecimal descuento = gross.subtract(shopifySubtotal);

        BigDecimal shipping = shopifyTotal.subtract(shopifySubtotal)
                .subtract(taxesIncluded ? BigDecimal.ZERO : tax)
                .subtract(tip);
        BigDecimal invoiceSubtotal = gross;

        if (shipping.compareTo(BigDecimal.ZERO) > 0) {
            items.add(extraLine(orderName, shippingItemCode, shipping));
            invoiceSubtotal = invoiceSubtotal.add(shipping);
        }
        if (tip.compareTo(BigDecimal.ZERO) > 0) {
            items.add(extraLine(orderName, tipItemCode, tip));
            invoiceSubtotal = invoiceSubtotal.add(tip);
        }

        // Con precios que incluyen impuesto, el impuesto ya está dentro de las líneas
        if (taxesIncluded) {
            invoiceSubtotal = invoiceSubtotal.subtract(tax);
        }

        invoice.put("Subtotal", invoiceSubtotal.doubleValue());
        invoice.put("Descuento", descuento.doubleValue());
        invoice.put("CityTax", cityTax.doubleValue());
        invoice.put("StateTax", stateTax.doubleValue());

        String error = amountsRuleError(invoice);
        if (error != null) {
            System.out.println("⚠️ Montos no cuadran para la regla del POS | Orden: " + orderName + " | " + error);
        }
    }

    /** Línea de factura sin impuesto ni descuento para un cargo que no es producto (envío, propina). */
    private Map<String, Object> extraLine(String orderName, String itemCode, BigDecimal amount) {
        Map<String, Object> line = new HashMap<>();
        line.put("FacturaNumero", "K" + orderName);
        line.put("Referencia", itemCode);
        line.put("CantidadOrdenada", 1);
        line.put("CantidadDespachada", 1);
        line.put("Precio", amount.doubleValue());
        line.put("Costo", 0.0);
        line.put("TotalF", amount.doubleValue());
        line.put("UnidadID", 1);
        line.put("Descuento", 0.0);
        line.put("CityTx", 0.0);
        line.put("StateTx", 0.0);
        return line;
    }

    /** @return null si la factura cumple Total == Subtotal - Descuento + CityTax + StateTax; si no, el detalle. */
    private String amountsRuleError(Map<String, Object> invoice) {
        BigDecimal total = BigDecimal.valueOf(((Number) invoice.get("Total")).doubleValue()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expected = BigDecimal.valueOf(((Number) invoice.get("Subtotal")).doubleValue())
                .subtract(BigDecimal.valueOf(((Number) invoice.get("Descuento")).doubleValue()))
                .add(BigDecimal.valueOf(((Number) invoice.get("CityTax")).doubleValue()))
                .add(BigDecimal.valueOf(((Number) invoice.get("StateTax")).doubleValue()))
                .setScale(2, RoundingMode.HALF_UP);
        if (expected.compareTo(total) == 0) {
            return null;
        }
        return "Montos no cuadran: Total=" + total + " | Subtotal-Descuento+Impuestos=" + expected;
    }

    /** Guarda (o actualiza) un pedido cuya factura no llegó a RM, para reintentarlo después. */
    private void recordFailedOrder(String orderId, JsonNode order, String error) {
        try {
            FailedOrder failed = failedOrderRepository.findById(orderId).orElseGet(() -> {
                FailedOrder f = new FailedOrder();
                f.setId(orderId);
                f.setFirstFailedAt(LocalDateTime.now());
                return f;
            });
            failed.setOrderName(order.path("name").asText(""));
            failed.setOrderJson(order.toString());
            failed.setAttempts(failed.getAttempts() + 1);
            failed.setLastError(error);
            failed.setLastAttemptAt(LocalDateTime.now());
            failedOrderRepository.save(failed);
            System.out.println("📝 Factura pendiente de reintento: " + orderId
                    + " (intento " + failed.getAttempts() + " de " + MAX_FAILED_ATTEMPTS + ")");
        } catch (Exception e) {
            System.out.println("❌ No se pudo guardar la factura fallida " + orderId + ": " + e.getMessage());
        }
    }

    private Map<String, Object> mapOrderFromQueryToInvoice(JsonNode order) {

    Map<String, Object> invoice = new HashMap<>();

    String orderName = order.path("name").asText("").replace("#", "");

    double total = order.path("totalPriceSet")
            .path("shopMoney")
            .path("amount").asDouble(0);

    // 🧾 HEADER
    invoice.put("Factura", "K" + orderName);
    invoice.put("OrdenNumero", orderName);
    invoice.put("Cliente", "0000");

    // 🔥 FECHAS (GraphQL usa createdAt)
    String createdAt = order.path("createdAt").asText();

    OffsetDateTime dateTime = OffsetDateTime.parse(createdAt);

    String fecha = dateTime.toLocalDate().toString();
    String hora = dateTime.toLocalDateTime().withNano(0).toString();

    invoice.put("Fecha", fecha);
    invoice.put("Hora", hora);

    invoice.put("Vendedor", 1);
    invoice.put("Via", 1);
    invoice.put("Terminos", 1);
    invoice.put("Transaccion", 50);
    invoice.put("Salesman", 1);
    invoice.put("EstadoActual", 1);

    invoice.put("ClienteOrdenNum", orderName);

    // Subtotal, Descuento e impuestos se calculan después de recorrer los ítems
    invoice.put("Total", total);

    // 🧾 ITEMS (GraphQL cambia estructura)
    List<Map<String, Object>> items = new ArrayList<>();

    int totalPiezas = 0;
    BigDecimal grossLines = BigDecimal.ZERO;

    for (JsonNode edge : order.path("lineItems").path("edges")) {

        JsonNode item = edge.path("node");

        double price = item.path("originalUnitPriceSet")
                .path("shopMoney")
                .path("amount").asDouble(0);

        int qty = item.path("quantity").asInt(0);

        totalPiezas += qty;
        grossLines = grossLines.add(BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(qty)));

        Map<String, Object> detail = new HashMap<>();
        String sku = item.path("variant").path("sku").asText();

if (sku == null || sku.isEmpty()) {
    sku = item.path("name").asText("SIN-SKU");
}

        detail.put("FacturaNumero", "K" + orderName);
        detail.put("Referencia", sku);
        detail.put("CantidadOrdenada", qty);
        detail.put("CantidadDespachada", qty);
        detail.put("Precio", price);
        detail.put("Costo", 0.0);
        detail.put("TotalF", price * qty);
        detail.put("UnidadID", 1);
        detail.put("Descuento", 0.0);
        detail.put("CityTx", 0.0);
        detail.put("StateTx", 0.0);

        items.add(detail);
    }

    // 💰 MONTOS: ver applyAmounts
    BigDecimal tax = money(order.path("totalTaxSet"));
    applyAmounts(invoice, items, orderName, grossLines,
            money(order.path("subtotalPriceSet")),
            money(order.path("totalPriceSet")),
            tax,
            order.path("taxesIncluded").asBoolean(false),
            cityTaxOf(order.path("taxLines")).min(tax),
            money(order.path("totalTipReceivedSet")));

    invoice.put("NumeroPiezas", totalPiezas);

    // 👤 Cliente
    JsonNode customer = order.path("customer");
    JsonNode billing = order.path("billingAddress");

    invoice.put("Nombre", customer.path("firstName").asText("Caja Registradora"));
    invoice.put("Telefono", customer.path("phone").asText(""));
    invoice.put("Direccion1", billing.path("address1").asText(""));

    // 💳 PAGOS DINÁMICOS
double paidCash = 0.0;
double paidDebit = 0.0;
double paidCredit = 0.0;
double paidCheck = 0.0;
double paidAth = 0.0;

JsonNode gateways = order.path("paymentGatewayNames");

// Si no viene info → fallback a efectivo
if (!gateways.isArray() || gateways.size() == 0) {
    paidCash = total;
} else {
    // El total se asigna una sola vez (a la primera pasarela reconocida): sin montos
    // por pasarela, sumarlo por cada una duplicaba el pago en órdenes con pago mixto
    boolean assigned = false;
    for (JsonNode gatewayNode : gateways) {

        String gateway = gatewayNode.asText("").toLowerCase();
        assigned = true;

        if (gateway.contains("visa") || gateway.contains("mastercard") || gateway.contains("credit")) {
            paidCredit = total;

        } else if (gateway.contains("debit")) {
            paidDebit = total;

        } else if (gateway.contains("cash")) {
            paidCash = total;

        } else if (gateway.contains("ath")) {
            paidAth = total;

        } else if (gateway.contains("check")) {
            paidCheck = total;

        } else {
            assigned = false;
        }

        if (assigned) {
            break;
        }
    }

    if (!assigned) {
        // 🔥 fallback inteligente
        paidCash = total;
    }
}

// Asignar al invoice
invoice.put("PaidCash", paidCash);
invoice.put("PaidDebitCard", paidDebit);
invoice.put("PaidCreditCard", paidCredit);
invoice.put("PaidCheck", paidCheck);
invoice.put("PaidAthMovil", paidAth);

    invoice.put("NCF", "");
    invoice.put("Puntos", 0.0);
    invoice.put("CompanyID", "1");

    invoice.put("Items", items);

    return invoice;
}

    private Map<String, Object> mapOrderToInvoice(JsonNode order) {

    Map<String, Object> invoice = new HashMap<>();

    // 🔹 Limpieza del número de orden (#9999 → 9999)
    String orderName = order.path("name").asText("").replace("#", "");

    double total = order.path("total_price").asDouble(0);

    // 🧾 HEADER
    invoice.put("Factura", "K" + orderName);
    invoice.put("OrdenNumero", orderName);
    invoice.put("Cliente", "0000");

    // ⚠️ POS suele querer fecha simple (no ISO completo)
    String createdAt = order.path("created_at").asText();

// Convertir formato Shopify → quitar zona horaria
OffsetDateTime dateTime = OffsetDateTime.parse(createdAt);

// Fecha (solo fecha)
String fecha = dateTime.toLocalDate().toString();

// Hora (datetime completo sin zona)
String hora = dateTime.toLocalDateTime().withNano(0).toString();

invoice.put("Fecha", fecha);
invoice.put("Hora", hora);

    invoice.put("Vendedor", 1);
    invoice.put("Via", 1);
    invoice.put("Terminos", 1);
    invoice.put("Transaccion", 50);
    invoice.put("Salesman", 1);
    invoice.put("EstadoActual", 1);

    invoice.put("ClienteOrdenNum", orderName);

    // Subtotal, Descuento e impuestos se calculan después de recorrer los ítems
    invoice.put("Total", total);

    // 🔢 Número de piezas
    invoice.put("NumeroPiezas", order.path("line_items").size());

    // 👤 Cliente
    JsonNode customer = order.path("customer");
    JsonNode billing = order.path("billing_address");

    invoice.put("Nombre", customer.path("first_name").asText("Caja Registradora"));
    invoice.put("Telefono", billing.path("phone").asText(""));
    invoice.put("Direccion1", billing.path("address1").asText(""));

    // 💳 PAGOS (simple por ahora)
    invoice.put("PaidCash", total);
    invoice.put("PaidDebitCard", 0.0);
    invoice.put("PaidCreditCard", 0.0);
    invoice.put("PaidCheck", 0.0);
    invoice.put("PaidAthMovil", 0.0);

    invoice.put("NCF", "");
    invoice.put("Puntos", 0.0);
    invoice.put("CompanyID", "1");

    // 🧾 ITEMS
    List<Map<String, Object>> items = new ArrayList<>();
    BigDecimal grossLines = BigDecimal.ZERO;

    for (JsonNode item : order.path("line_items")) {

        double price = item.path("price").asDouble(0);
        int qty = item.path("quantity").asInt(0);
        grossLines = grossLines.add(BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(qty)));

        Map<String, Object> detail = new HashMap<>();

        detail.put("FacturaNumero", "K" + orderName);

        // 🔥 Usa SKU si existe
        String sku = item.path("sku").asText();
        if (sku == null || sku.isEmpty()) {
            sku = item.path("name").asText("SIN-SKU");
        }

        detail.put("Referencia", sku);

        detail.put("CantidadOrdenada", qty);
        detail.put("CantidadDespachada", qty);
        detail.put("Precio", price);
        detail.put("Costo", 0.0);
        detail.put("TotalF", price * qty);
        detail.put("UnidadID", 1);
        detail.put("Descuento", 0.0);

        // 🔥 Impuestos por ítem (simple)
        detail.put("CityTx", 0.0);
        detail.put("StateTx", 0.0);

        items.add(detail);
    }

    // 💰 MONTOS: misma regla que mapOrderFromQueryToInvoice, con los campos del webhook (REST)
    BigDecimal tax = decimal(order.path("total_tax"));
    applyAmounts(invoice, items, orderName, grossLines,
            decimal(order.path("subtotal_price")),
            decimal(order.path("total_price")),
            tax,
            order.path("taxes_included").asBoolean(false),
            cityTaxOfWebhook(order.path("tax_lines")).min(tax),
            decimal(order.path("total_tip_received")));

    invoice.put("Items", items);

    return invoice;
}
    /**
     * Vista previa: arma la factura de un pedido de Shopify igual que syncOrdersToPOS,
     * pero no la envía a RM ni escribe en ProcessedOrder.
     * Incluye si cumple la regla de montos de RM API y si el pedido ya fue procesado.
     */
    public Map<String, Object> previewInvoice(String orderId) throws Exception {
        JsonNode order = new ObjectMapper()
                .readTree(shopifyService.getOrderById(orderId))
                .path("data")
                .path("order");

        if (order.isMissingNode() || order.isNull()) {
            throw new IllegalArgumentException("No se encontró la orden en Shopify: " + orderId);
        }

        Map<String, Object> invoice = mapOrderFromQueryToInvoice(order);
        String amountsError = amountsRuleError(invoice);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.path("id").asText());
        result.put("orderName", order.path("name").asText());
        result.put("financialStatus", order.path("displayFinancialStatus").asText());
        // Solo los pedidos PAID se envían a RM en la sincronización
        result.put("wouldSend", "PAID".equals(order.path("displayFinancialStatus").asText()));
        result.put("alreadyProcessed", repository.existsById(order.path("id").asText()));
        result.put("failedAttempts", failedOrderRepository.findById(order.path("id").asText())
                .map(FailedOrder::getAttempts).orElse(0));
        result.put("ruleOk", amountsError == null);
        result.put("ruleError", amountsError);
        result.put("invoice", invoice);
        return result;
    }

    /**
     * Reintenta las facturas guardadas en FailedOrder que no llegan al máximo de intentos.
     * Se vuelven a mapear desde el pedido guardado, así que toman las correcciones del código
     * y los productos creados en RM después del fallo. En simulación no hace nada.
     */
    public void retryFailedOrders() {
        if (invoiceDryRun) {
            System.out.println("🧪 SIMULACIÓN activa: no se reintentan facturas fallidas");
            return;
        }
        List<FailedOrder> pending = failedOrderRepository.findByAttemptsLessThanOrderByFirstFailedAtAsc(MAX_FAILED_ATTEMPTS);
        if (pending.isEmpty()) {
            return;
        }
        System.out.println("🔁 Reintentando facturas fallidas: " + pending.size());
        for (FailedOrder failed : pending) {
            retryFailedOrder(failed);
        }
    }

    /** Reintenta una sola factura fallida. @return true si quedó enviada (o ya existía en RM). */
    public boolean retryFailedOrder(FailedOrder failed) {
        String orderId = failed.getId();

        // Ya procesada por otro camino (webhook o un ciclo anterior)
        if (repository.existsById(orderId)) {
            failedOrderRepository.delete(failed);
            return true;
        }

        JsonNode order;
        try {
            order = new ObjectMapper().readTree(failed.getOrderJson());
        } catch (Exception e) {
            recordFailedOrder(orderId, new ObjectMapper().createObjectNode().put("name", failed.getOrderName()),
                    "Pedido guardado ilegible: " + e.getMessage());
            return false;
        }

        try {
            Map<String, Object> invoice = mapOrderFromQueryToInvoice(order);
            String amountsError = amountsRuleError(invoice);
            if (amountsError != null) {
                throw new IllegalStateException(amountsError);
            }
            posClientService.sendInvoiceToPOS(invoice);
            markProcessed(orderId, order);
            failedOrderRepository.delete(failed);
            System.out.println("✅ Factura reintentada y enviada: " + orderId);
            return true;

        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("Invoice already exists")) {
                markProcessed(orderId, order);
                failedOrderRepository.delete(failed);
                System.out.println("✅ Factura ya existía en RM: " + orderId);
                return true;
            }
            System.out.println("❌ Reintento fallido: " + orderId + " | " + message);
            recordFailedOrder(orderId, order, message);
            if (failed.getAttempts() + 1 >= MAX_FAILED_ATTEMPTS) {
                System.out.println("⛔ Factura " + orderId + " llegó a " + MAX_FAILED_ATTEMPTS
                        + " intentos: queda para revisión en el panel");
            }
            return false;
        }
    }

    private void markProcessed(String orderId, JsonNode order) {
        repository.save(new ProcessedOrder(
                orderId,
                LocalDateTime.now(),
                order.toString(),
                resolveEventType(order),
                money(order.path("totalPriceSet"))));
    }

    public void syncOrdersToPOS(String lastSyncDate) throws Exception {
     System.out.println("📦 Sincronizando ordenes...");
    // 🔥 PAGINACIÓN: Shopify entrega máximo 50 órdenes por página
    String cursor = null;
    int page = 0;
    do {
    String response = shopifyService.getNewOrders(lastSyncDate, cursor);

    JsonNode ordersNode = new ObjectMapper()
            .readTree(response)
            .path("data")
            .path("orders");
    JsonNode orders = ordersNode.path("edges");
        System.out.println("📦 Ordenes recibidas: " + orders.size() + " (página " + (page + 1) + ")");
    for (JsonNode edge : orders) {

        JsonNode order = edge.path("node");

        String orderId = order.path("id").asText();

        if (orderId == null || orderId.isEmpty()) {
            continue;
        }

        // 🔥 ESTADO FINANCIERO
        String financialStatus = order.path("displayFinancialStatus").asText();

        // 🔥 VALIDACIÓN DUPLICADOS
        if (repository.existsById(orderId)) {

            System.out.println("⚠️ Orden duplicada: " + orderId);
            continue;
        }

        try {
            String eventType = resolveEventType(order);
            BigDecimal total =
    new BigDecimal(
        order
            .path("totalPriceSet")
            .path("shopMoney")
            .path("amount")
            .asText("0")
    );
            // 🔥 PROTECCIÓN DUPLICADOS
            repository.save(
                new ProcessedOrder(
                    orderId,
                    LocalDateTime.now(),
                    order.toString(),
                    eventType, total
                )
            );

        } catch (Exception e) {

            System.out.println("⚠️ Orden duplicada: " + orderId);
            continue;
        }

        try {

            switch (financialStatus) {

                // ✅ VENTA NORMAL
                case "PAID":

                    // 🧾 MAPEAR
                    Map<String, Object> invoice =
                            mapOrderFromQueryToInvoice(order);
                    String amountsError = amountsRuleError(invoice);

                    // 🧪 SIMULACIÓN: solo log, sin enviar ni marcar como procesada
                    if (invoiceDryRun) {
                        System.out.println("🧪 SIMULACIÓN | Orden " + orderId
                                + (amountsError == null ? " | montos OK" : " | " + amountsError)
                                + " | Factura: " + new ObjectMapper().writeValueAsString(invoice));
                        repository.deleteById(orderId);
                        break;
                    }

                    // 🚫 Montos que RM rechazaría: no se envía, queda para reintento
                    if (amountsError != null) {
                        System.out.println("❌ Factura no enviada: " + orderId + " | " + amountsError);
                        repository.deleteById(orderId);
                        recordFailedOrder(orderId, order, amountsError);
                        break;
                    }

                    // 🚀 ENVIAR
                    posClientService.sendInvoiceToPOS(invoice);

                    System.out.println("✅ Orden PAID procesada: " + orderId);

                    break;

                // 🔥 PAGO PARCIAL
                case "PARTIALLY_PAID":

                    // TODO:
                    // manejar orden parcialmente pagada

                    System.out.println("⚠️ Orden PARTIALLY_PAID detectada: " + orderId);
repository.deleteById(orderId);
                    break;

                // 🔥 REEMBOLSO PARCIAL
                case "PARTIALLY_REFUNDED":

                    // TODO:
                    // manejar devolución parcial

                    System.out.println("⚠️ Orden PARTIALLY_REFUNDED detectada: " + orderId);
repository.deleteById(orderId);
                    break;

                // 🔥 REEMBOLSO TOTAL
                case "REFUNDED":

                    // TODO:
                    // revertir venta / generar nota crédito

                    System.out.println("⚠️ Orden REFUNDED detectada: " + orderId);
repository.deleteById(orderId);
                    break;

                // 🔥 ANULADA
                case "VOIDED":

                    // TODO:
                    // manejar orden anulada

                    System.out.println("⚠️ Orden VOIDED detectada: " + orderId);
repository.deleteById(orderId);
                    break;

                default:

                    System.out.println(
                        "⚠️ Estado financiero no manejado: "
                        + financialStatus
                        + " | Orden: "
                        + orderId
                    );
                    repository.deleteById(orderId);
                    break;
            }

        } catch (Exception e) {

            String message = e.getMessage();

            System.out.println("❌ Error enviando orden: " + orderId);
            System.out.println("❌ Error: " + e.getMessage());

            // 🔥 ROLLBACK MANUAL
            if (message == null ||
                !message.contains("Invoice already exists")) {

                repository.deleteById(orderId);

                // 📝 Shopify se consulta por fecha de creación: sin esto el pedido no se vuelve a leer
                if ("PAID".equals(financialStatus)) {
                    recordFailedOrder(orderId, order, message);
                }
            }
        }
    }

    JsonNode pageInfo = ordersNode.path("pageInfo");
    cursor = pageInfo.path("hasNextPage").asBoolean(false)
            ? pageInfo.path("endCursor").asText(null)
            : null;
    page++;
    } while (cursor != null && page < MAX_ORDER_PAGES);

    if (cursor != null) {
        System.out.println("⚠️ Se alcanzó el límite de " + MAX_ORDER_PAGES + " páginas de órdenes; quedan órdenes sin leer");
    }
}
private String resolveEventType(JsonNode order) {

    String financialStatus =
        order.path("displayFinancialStatus").asText("");

    // El webhook (API REST) usa financial_status y cancelled_at en lugar de los campos de GraphQL
    if (financialStatus.isEmpty()) {
        financialStatus = order.path("financial_status").asText("");
    }

    boolean cancelled =
        (!order.path("cancelledAt").isNull()
        && !order.path("cancelledAt").asText().isEmpty())
        || (!order.path("cancelled_at").isNull()
        && !order.path("cancelled_at").asText().isEmpty());

    if (cancelled) {
        return "CANCELLED";
    }

    switch (financialStatus.toUpperCase()) {

        case "PAID":
            return "PAID";

        case "PARTIALLY_PAID":
            return "PARTIALLY_PAID";

        case "PARTIALLY_REFUNDED":
            return "PARTIALLY_REFUNDED";

        case "REFUNDED":
            return "REFUNDED";

        case "VOIDED":
            return "VOIDED";

        case "PENDING":
            return "PENDING";

        default:
            return "UNKNOWN";
    }
}

    private boolean verifyHmac(String data, String hmacHeader) {
        try {
            String secret = webhookSettingsService.getSecret(webhookSecret);

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKey);

            byte[] rawHmac = mac.doFinal(data.getBytes());
            String calculatedHmac = Base64.getEncoder().encodeToString(rawHmac);

            return calculatedHmac.equals(hmacHeader);

        } catch (Exception e) {
            throw new RuntimeException("Error validando HMAC", e);
        }
    }

    public ResponseEntity<?> receiveOrder(
        String hmac,
        String payload) {

    System.out.println("📦 Payload recibido: " + payload);

    try {

        // TODO: DESCOMENTAR EN PRODUCCION
        // 🔐 Validar Shopify
        
        if (!verifyHmac(payload, hmac)) {
            WebhookTrace.hmacValid(false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        WebhookTrace.hmacValid(true);

        System.out.println("✅ Webhook recibido");

        // 🔄 Convertir JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode order = mapper.readTree(payload);

        // 🔑 ID ORDEN
        String orderId = order.path("admin_graphql_api_id").asText();

        if (orderId == null || orderId.isEmpty()) {
            throw new RuntimeException("Order ID inválido");
        }

        WebhookTrace.order(orderId, order.path("name").asText(""));

        // 🔥 ESTADO FINANCIERO
        String financialStatus = order.path("financial_status").asText();

        // 🚫 SOLO PROCESAR PAGADAS
        if (!"paid".equalsIgnoreCase(financialStatus)) {

            System.out.println(
                "⚠️ Orden ignorada por estado financiero: "
                + financialStatus
            );
            WebhookTrace.result("IGNORADA", "Solo se facturan pedidos pagados; estado financiero: " + financialStatus);

            return ResponseEntity.ok().build();
        }

        // 🚫 DUPLICADOS
        if (repository.existsById(orderId)) {

            System.out.println("⚠️ Orden duplicada: " + orderId);
            WebhookTrace.result("DUPLICADA", "El pedido ya estaba procesado; no se envió otra factura a RM");

            return ResponseEntity.ok().build();
        }

        // 🧾 MAPEAR
        Map<String, Object> invoice = mapOrderToInvoice(order);
        String eventType = resolveEventType(order);
        WebhookTrace.invoice(invoice);
        // 🚀 ENVIAR POS
        posClientService.sendInvoiceToPOS(invoice);
         BigDecimal total = new BigDecimal(
            order.path("total_price").asText("0")
        );
        // 💾 GUARDAR PROCESADA
        repository.save(
            new ProcessedOrder(
                orderId,
                LocalDateTime.now(),
                payload,
                eventType, 
                total
            )
        );

        System.out.println("✅ Orden procesada correctamente: " + orderId);
        WebhookTrace.result("PROCESADA", "Factura " + invoice.get("Factura") + " enviada a RM; pedido guardado en ProcessedOrder como "
                + eventType + " por " + total);

        HashMap<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("success", true);

        return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (Exception e) {

        e.printStackTrace();
        WebhookTrace.error(e);
        WebhookTrace.result("ERROR", e.getMessage());

        HashMap<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
public ResponseEntity<?> receiveCancelledOrder(
        String hmac,
        String payload) {

    System.out.println("📦 Cancelled payload recibido: " + payload);

    try {

        // 🔐 VALIDAR SHOPIFY
        if (!verifyHmac(payload, hmac)) {
            WebhookTrace.hmacValid(false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        WebhookTrace.hmacValid(true);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode order = mapper.readTree(payload);

        String orderId = order.path("admin_graphql_api_id").asText();

        if (orderId == null || orderId.isEmpty()) {
            throw new RuntimeException("Order ID inválido");
        }

        WebhookTrace.order(orderId, order.path("name").asText(""));

        // 🔥 VALIDAR SI EXISTE EN POS
        boolean exists = repository.existsById(orderId);

        if (!exists) {

            System.out.println(
                "⚠️ Orden cancelada ignorada porque no existe en POS: "
                + orderId
            );
            WebhookTrace.result("IGNORADA", "Cancelación de un pedido que no se había enviado a RM");

            return ResponseEntity.ok().build();
        }

        // TODO:
        // 🔥 ANULAR FACTURA EN POS

        // TODO:
        // 🔥 RESTAURAR INVENTARIO SI APLICA

        System.out.println("✅ Orden cancelada procesada: " + orderId);
        WebhookTrace.result("RECIBIDA", "Cancelación registrada. Pendiente: anular la factura en RM y restaurar inventario (aún no implementado)");

        HashMap<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("orderId", orderId);

        return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (Exception e) {

        e.printStackTrace();
        WebhookTrace.error(e);
        WebhookTrace.result("ERROR", e.getMessage());

        HashMap<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
public ResponseEntity<?> receiveRefund(
        String hmac,
        String payload) {

    System.out.println("📦 Refund payload recibido: " + payload);

    try {

        // 🔐 VALIDAR SHOPIFY
        if (!verifyHmac(payload, hmac)) {
            WebhookTrace.hmacValid(false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        WebhookTrace.hmacValid(true);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode refund = mapper.readTree(payload);

        // 🔥 ORDER ID
        String orderId = refund.path("order_id").asText();

        if (orderId == null || orderId.isEmpty()) {
            throw new RuntimeException("Order ID inválido");
        }

        String shopifyOrderId =
                "gid://shopify/Order/" + orderId;

        WebhookTrace.order(shopifyOrderId, null);

        // 🔥 VALIDAR EXISTE EN POS
        Optional<ProcessedOrder> processedOrderOpt =
                repository.findById(shopifyOrderId);

        if (processedOrderOpt.isEmpty()) {

            System.out.println(
                "⚠️ Refund ignorado porque orden no existe en POS: "
                + orderId
            );
            WebhookTrace.result("IGNORADA", "Reembolso de un pedido que no se había enviado a RM");

            return ResponseEntity.ok().build();
        }

        // 🔥 PAYLOAD ORIGINAL
        ProcessedOrder processedOrder =
                processedOrderOpt.get();

       

        // 🔥 TOTAL ORIGINAL
        double originalTotal =
                processedOrder.getTotalAmount().doubleValue();

        // 🔥 TOTAL REFUND
        double refundTotal = 0;

        JsonNode transactions = refund.path("transactions");

        if (transactions.isArray()) {

            for (JsonNode tx : transactions) {

                String kind = tx.path("kind").asText();

                if ("refund".equalsIgnoreCase(kind)) {

                    refundTotal += tx.path("amount").asDouble();
                }
            }
        }

        System.out.println("💰 Original total: " + originalTotal);
        System.out.println("💸 Refund total: " + refundTotal);

        // 🔥 FULL O PARTIAL
        boolean fullRefund =
                refundTotal >= originalTotal;

        if (fullRefund) {

            // TODO:
            // 🔥 PROCESAR REFUND TOTAL EN POS

            System.out.println(
                "⚠️ FULL REFUND detectado para orden: "
                + orderId
            );

        } else {

            // TODO:
            // 🔥 PROCESAR REFUND PARCIAL EN POS

            System.out.println(
                "⚠️ PARTIAL REFUND detectado para orden: "
                + orderId
            );
        }

        WebhookTrace.result("RECIBIDA", (fullRefund ? "Reembolso total" : "Reembolso parcial") + " de " + refundTotal
                + " sobre un total de " + originalTotal + ". Pendiente: procesarlo en RM (aún no implementado)");

        HashMap<String, Object> response =
                new HashMap<>();

        response.put("success", true);
        response.put("orderId", orderId);
        response.put("refundTotal", refundTotal);
        response.put("fullRefund", fullRefund);

        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );

    } catch (Exception e) {

        e.printStackTrace();
        WebhookTrace.error(e);
        WebhookTrace.result("ERROR", e.getMessage());

        HashMap<String, Object> response =
                new HashMap<>();

        response.put("success", false);
        response.put("error", e.getMessage());

        return new ResponseEntity<>(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
}
