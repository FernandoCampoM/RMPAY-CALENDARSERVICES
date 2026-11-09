package com.retailmanager.rmpayCalendar.services.services.webhook;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpayCalendar.db2.entity.WebhookLog;

/**
 * Anotaciones de la petición de webhook en curso. WebhookLogFilter abre la traza al recibir la
 * petición y la guarda al responder; el código que la atiende (InvoiceSyncService) va anotando
 * aquí lo que hace. Fuera de un webhook (por ejemplo en la sincronización por consulta) no hay
 * traza abierta y todos los métodos no hacen nada. Nunca lanzan excepción.
 */
public final class WebhookTrace {

    private static final ThreadLocal<WebhookLog> CURRENT = new ThreadLocal<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private WebhookTrace() {
    }

    static void begin(WebhookLog log) {
        CURRENT.set(log);
    }

    static WebhookLog end() {
        WebhookLog log = CURRENT.get();
        CURRENT.remove();
        return log;
    }

    public static void hmacValid(boolean valid) {
        WebhookLog log = CURRENT.get();
        if (log != null) {
            log.setHmacValid(valid);
        }
    }

    public static void order(String shopifyOrderId, String orderName) {
        WebhookLog log = CURRENT.get();
        if (log != null) {
            log.setShopifyOrderId(shopifyOrderId);
            log.setOrderName(orderName);
        }
    }

    /** Resultado (PROCESADA, DUPLICADA, IGNORADA, ...) y qué se hizo o se guardó. */
    public static void result(String result, String detail) {
        WebhookLog log = CURRENT.get();
        if (log != null) {
            log.setResult(result);
            log.setDetail(detail);
        }
    }

    /** Factura que se envía a RM. */
    public static void invoice(Object invoice) {
        WebhookLog log = CURRENT.get();
        if (log == null) {
            return;
        }
        try {
            log.setInvoiceJson(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(invoice));
        } catch (Exception e) {
            log.setInvoiceJson(String.valueOf(invoice));
        }
    }

    public static void error(Throwable e) {
        WebhookLog log = CURRENT.get();
        if (log == null || e == null) {
            return;
        }
        log.setError(e.getClass().getSimpleName() + ": " + e.getMessage());
        StringWriter stack = new StringWriter();
        e.printStackTrace(new PrintWriter(stack));
        log.setStackTrace(stack.toString());
    }
}
