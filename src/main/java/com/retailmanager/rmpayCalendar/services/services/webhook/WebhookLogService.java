package com.retailmanager.rmpayCalendar.services.services.webhook;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpayCalendar.db2.entity.WebhookLog;
import com.retailmanager.rmpayCalendar.db2.repository.WebhookLogRepository;

/**
 * Guarda la traza de los webhooks (WebhookLog) y borra la que tiene más de
 * webhook.log.retention-days días: al arrancar y, como mucho, cada 6 horas al guardar.
 * Con retention-days = 0 no se borra nada. Un error al guardar nunca afecta al webhook.
 */
@Service
public class WebhookLogService {

    private static final Duration PURGE_INTERVAL = Duration.ofHours(6);

    @Autowired
    private WebhookLogRepository repository;

    @Value("${webhook.log.retention-days:90}")
    private int retentionDays;

    private volatile LocalDateTime lastPurge = LocalDateTime.MIN;

    public void save(WebhookLog log) {
        try {
            repository.save(log);
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo guardar la traza del webhook " + log.getEndpoint() + ": " + e.getMessage());
        }
        if (LocalDateTime.now().isAfter(lastPurge.plus(PURGE_INTERVAL))) {
            purge();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void purge() {
        lastPurge = LocalDateTime.now();
        if (retentionDays <= 0) {
            return;
        }
        try {
            int deleted = repository.deleteOlderThan(LocalDateTime.now().minusDays(retentionDays));
            if (deleted > 0) {
                System.out.println("🧹 Traza de webhooks: " + deleted + " registros de más de " + retentionDays + " días eliminados");
            }
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo depurar la traza de webhooks: " + e.getMessage());
        }
    }
}
