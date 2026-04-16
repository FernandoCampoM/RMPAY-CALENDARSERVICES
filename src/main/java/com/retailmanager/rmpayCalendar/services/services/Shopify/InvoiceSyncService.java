package com.retailmanager.rmpayCalendar.services.services.Shopify;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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

    private Map<String, Object> mapOrderToInvoice(JsonNode order) {

        Map<String, Object> invoice = new HashMap<>();

        invoice.put("Factura", order.path("name").asText());
        invoice.put("OrdenNumero", order.path("name").asText());
        invoice.put("Cliente", "0000");

        invoice.put("Fecha", order.path("created_at").asText());
        invoice.put("Hora", order.path("created_at").asText());

        invoice.put("Subtotal", order.path("subtotal_price").asDouble());
        invoice.put("Descuento", order.path("total_discounts").asDouble());
        invoice.put("Total", order.path("total_price").asDouble());

        invoice.put("Nombre", order.path("customer").path("first_name").asText(""));
        invoice.put("Telefono", order.path("customer").path("phone").asText(""));
        invoice.put("Direccion1", order.path("billing_address").path("address1").asText(""));

        // 💳 PAGOS
        double total = order.path("total_price").asDouble();
        invoice.put("PaidCash", total); // puedes mejorar luego

        // 🧾 ITEMS
        List<Map<String, Object>> items = new ArrayList<>();

        for (JsonNode item : order.path("line_items")) {

            double price = item.path("price").asDouble();
            int qty = item.path("quantity").asInt();

            Map<String, Object> detail = new HashMap<>();
            detail.put("Nombre", item.path("name").asText());
            detail.put("CantidadOrdenada", qty);
            detail.put("CantidadDespachada", qty);
            detail.put("Precio", price);
            detail.put("TotalF", price * qty);

            items.add(detail);
        }

        invoice.put("Items", items);

        return invoice;
    }

    public void syncOrdersToPOS() throws JsonMappingException, JsonProcessingException {

        String response = shopifyService.getRecentOrders();

        JsonNode orders = new ObjectMapper()
                .readTree(response)
                .path("data")
                .path("orders")
                .path("edges");

        for (JsonNode edge : orders) {

            JsonNode order = edge.path("node");

            Map<String, Object> invoice = mapOrderToInvoice(order);

            posClientService.sendInvoiceToPOS(invoice);
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
            // 🔐 1. Validar que viene de Shopify
            
             if (!verifyHmac(payload, hmac)) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
             }

            System.out.println("✅ Webhook recibido");

            // 🔄 2. Convertir JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode order = mapper.readTree(payload);

            // 🔑 3. Obtener ID único de la orden
            String orderId = order.path("admin_graphql_api_id").asText();

            if (orderId == null || orderId.isEmpty()) {
                throw new RuntimeException("Order ID inválido");
            }

            // 🚫 4. Validar duplicados
            if (repository.existsById(orderId)) {
                System.out.println("⚠️ Orden duplicada: " + orderId);
                return ResponseEntity.ok().build();
            }

            // 🧾 5. Mapear a factura
            Map<String, Object> invoice = mapOrderToInvoice(order);

            // 🚀 6. Enviar al POS
            posClientService.sendInvoiceToPOS(invoice);

            // 💾 7. Marcar como procesada (SOLO si todo salió bien)
            repository.save(new ProcessedOrder(orderId, LocalDateTime.now()));

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
}
