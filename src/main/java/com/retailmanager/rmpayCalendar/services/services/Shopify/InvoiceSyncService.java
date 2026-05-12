package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.io.OutputStream;
import java.math.BigDecimal;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpayCalendar.db2.entity.ProcessedOrder;
import com.retailmanager.rmpayCalendar.db2.repository.ProcessedOrderRepository;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncRepository;
import com.retailmanager.rmpayCalendar.db2.repository.Sys_general_configRepository;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InvoiceSyncService {
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
    private Map<String, Object> mapOrderFromQueryToInvoice(JsonNode order) {

    Map<String, Object> invoice = new HashMap<>();

    String orderName = order.path("name").asText("").replace("#", "");

    double subtotal = order.path("subtotalPriceSet")
            .path("shopMoney")
            .path("amount").asDouble(0);

    double total = order.path("totalPriceSet")
            .path("shopMoney")
            .path("amount").asDouble(0);

    double discount = order.path("totalDiscountsSet")
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

    invoice.put("Subtotal", subtotal);
    invoice.put("Descuento", discount);
    invoice.put("Total", total);

    invoice.put("CityTax", 0.0);
    invoice.put("StateTax", 0.0);

    // 🧾 ITEMS (GraphQL cambia estructura)
    List<Map<String, Object>> items = new ArrayList<>();

    int totalPiezas = 0;

    for (JsonNode edge : order.path("lineItems").path("edges")) {

        JsonNode item = edge.path("node");

        double price = item.path("originalUnitPriceSet")
                .path("shopMoney")
                .path("amount").asDouble(0);

        int qty = item.path("quantity").asInt(0);

        totalPiezas += qty;

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
    for (JsonNode gatewayNode : gateways) {

        String gateway = gatewayNode.asText("").toLowerCase();

        if (gateway.contains("visa") || gateway.contains("mastercard") || gateway.contains("credit")) {
            paidCredit += total;

        } else if (gateway.contains("debit")) {
            paidDebit += total;

        } else if (gateway.contains("cash")) {
            paidCash += total;

        } else if (gateway.contains("ath")) {
            paidAth += total;

        } else if (gateway.contains("check")) {
            paidCheck += total;

        } else {
            // 🔥 fallback inteligente
            paidCash += total;
        }
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

    double subtotal = order.path("subtotal_price").asDouble(0);
    double discount = order.path("total_discounts").asDouble(0);
    double total = order.path("total_price").asDouble(0);
    double tax = order.path("total_tax").asDouble(0);

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

    invoice.put("Subtotal", subtotal);
    invoice.put("Descuento", discount);
    invoice.put("Total", total);

    // 🔥 Impuestos
    invoice.put("CityTax", 0.0);
    invoice.put("StateTax", tax);

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

    for (JsonNode item : order.path("line_items")) {

        double price = item.path("price").asDouble(0);
        int qty = item.path("quantity").asInt(0);

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

    invoice.put("Items", items);

    return invoice;
}
    public void syncOrdersToPOS(String lastSyncDate) throws Exception {
     System.out.println("📦 Sincronizando ordenes...");   
    String response = shopifyService.getNewOrders(lastSyncDate);

    JsonNode orders = new ObjectMapper()
            .readTree(response)
            .path("data")
            .path("orders")
            .path("edges");
        System.out.println("📦 Ordenes recibidas: " + orders.size());
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
            }
        }
    }
}
private String resolveEventType(JsonNode order) {

    String financialStatus =
        order.path("displayFinancialStatus").asText("");

    boolean cancelled =
        !order.path("cancelledAt").isNull()
        && !order.path("cancelledAt").asText().isEmpty();

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
            String secret = "6517da6a31a9fb849cd904925b6ee47ac53ee90573fde7937d23bf17fdcbe8bf"; // ⚠️ sacar de Shopify

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        

        System.out.println("✅ Webhook recibido");

        // 🔄 Convertir JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode order = mapper.readTree(payload);

        // 🔑 ID ORDEN
        String orderId = order.path("admin_graphql_api_id").asText();

        if (orderId == null || orderId.isEmpty()) {
            throw new RuntimeException("Order ID inválido");
        }

        // 🔥 ESTADO FINANCIERO
        String financialStatus = order.path("financial_status").asText();

        // 🚫 SOLO PROCESAR PAGADAS
        if (!"paid".equalsIgnoreCase(financialStatus)) {

            System.out.println(
                "⚠️ Orden ignorada por estado financiero: "
                + financialStatus
            );

            return ResponseEntity.ok().build();
        }

        // 🚫 DUPLICADOS
        if (repository.existsById(orderId)) {

            System.out.println("⚠️ Orden duplicada: " + orderId);

            return ResponseEntity.ok().build();
        }

        // 🧾 MAPEAR
        Map<String, Object> invoice = mapOrderToInvoice(order);
        String eventType = resolveEventType(order);
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

        HashMap<String, Object> response = new HashMap<>();
        response.put("orderId", orderId);
        response.put("success", true);

        return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (Exception e) {

        e.printStackTrace();

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode order = mapper.readTree(payload);

        String orderId = order.path("admin_graphql_api_id").asText();

        if (orderId == null || orderId.isEmpty()) {
            throw new RuntimeException("Order ID inválido");
        }

        // 🔥 VALIDAR SI EXISTE EN POS
        boolean exists = repository.existsById(orderId);

        if (!exists) {

            System.out.println(
                "⚠️ Orden cancelada ignorada porque no existe en POS: "
                + orderId
            );

            return ResponseEntity.ok().build();
        }

        // TODO:
        // 🔥 ANULAR FACTURA EN POS

        // TODO:
        // 🔥 RESTAURAR INVENTARIO SI APLICA

        System.out.println("✅ Orden cancelada procesada: " + orderId);

        HashMap<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("orderId", orderId);

        return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (Exception e) {

        e.printStackTrace();

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode refund = mapper.readTree(payload);

        // 🔥 ORDER ID
        String orderId = refund.path("order_id").asText();

        if (orderId == null || orderId.isEmpty()) {
            throw new RuntimeException("Order ID inválido");
        }

        String shopifyOrderId =
                "gid://shopify/Order/" + orderId;

        // 🔥 VALIDAR EXISTE EN POS
        Optional<ProcessedOrder> processedOrderOpt =
                repository.findById(shopifyOrderId);

        if (processedOrderOpt.isEmpty()) {

            System.out.println(
                "⚠️ Refund ignorado porque orden no existe en POS: "
                + orderId
            );

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
