package com.retailmanager.rmpayCalendar.services.services.webhook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.retailmanager.rmpayCalendar.db2.entity.WebhookLog;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Registra en WebhookLog cada petición a /webhook/* (Shopify): headers, body, respuesta,
 * duración y lo que anote el código con WebhookTrace. Va como filtro para registrar también
 * las peticiones que Spring rechaza antes de llegar al controlador (por ejemplo sin firma).
 * No cambia la petición ni la respuesta. Depende de WebhookLogService.
 */
@Configuration
public class WebhookLogFilter {

    @Autowired
    private WebhookLogService webhookLogService;

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> webhookTraceFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
                ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

                WebhookLog log = new WebhookLog();
                log.setReceivedAt(LocalDateTime.now());
                log.setEndpoint(request.getRequestURI());
                log.setTopic(request.getHeader("X-Shopify-Topic"));
                log.setWebhookId(request.getHeader("X-Shopify-Webhook-Id"));
                log.setShopDomain(request.getHeader("X-Shopify-Shop-Domain"));
                String hmac = request.getHeader("X-Shopify-Hmac-Sha256");
                log.setHmacPresent(hmac != null && !hmac.isBlank());

                long start = System.currentTimeMillis();
                WebhookTrace.begin(log);
                try {
                    chain.doFilter(req, res);
                } catch (IOException | ServletException | RuntimeException e) {
                    WebhookTrace.error(e);
                    throw e;
                } finally {
                    WebhookTrace.end();
                    log.setDurationMs(System.currentTimeMillis() - start);
                    log.setHttpStatus(res.getStatus());
                    log.setPayload(body(req));
                    log.setResponseBody(new String(res.getContentAsByteArray(), StandardCharsets.UTF_8));
                    res.copyBodyToResponse();
                    if (log.getResult() == null) {
                        log.setResult(resultFromStatus(res.getStatus(), log));
                    }
                    webhookLogService.save(log);
                }
            }
        });
        registration.addUrlPatterns("/webhook/*");
        registration.setName("webhookTraceFilter");
        return registration;
    }

    /** Body recibido; si Spring no llegó a leerlo (petición rechazada antes), se lee aquí. */
    private static String body(ContentCachingRequestWrapper req) {
        byte[] bytes = req.getContentAsByteArray();
        if (bytes.length == 0) {
            try {
                bytes = req.getInputStream().readAllBytes();
            } catch (Exception e) {
                return "";
            }
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String resultFromStatus(int status, WebhookLog log) {
        if (status >= 400 && !Boolean.TRUE.equals(log.getHmacPresent())) {
            return "SIN_FIRMA";
        }
        if (status == 401) {
            return "FIRMA_INVALIDA";
        }
        if (status >= 400 && status < 500) {
            return "PETICION_INVALIDA";
        }
        if (status >= 500) {
            return "ERROR";
        }
        return "OK";
    }
}
