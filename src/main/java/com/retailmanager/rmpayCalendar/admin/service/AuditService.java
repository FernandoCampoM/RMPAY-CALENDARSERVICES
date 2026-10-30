package com.retailmanager.rmpayCalendar.admin.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpayCalendar.db2.entity.AdminAuditLog;
import com.retailmanager.rmpayCalendar.db2.repository.AdminAuditLogRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Registra en AdminAuditLog las acciones hechas desde el panel admin y por el sync cuando aplica.
 * Nunca lanza excepción: un fallo de auditoría no debe detener la acción principal.
 */
@Slf4j
@Service
public class AuditService {

    @Autowired
    private AdminAuditLogRepository repository;

    public void log(String username, String action, String detail) {
        try {
            AdminAuditLog entry = new AdminAuditLog();
            entry.setAt(LocalDateTime.now());
            entry.setUsername(username == null ? "sistema" : username);
            entry.setAction(action);
            entry.setDetail(detail);
            repository.save(entry);
        } catch (Exception e) {
            log.error("AUDIT_ERROR | action={} | error={}", action, e.getMessage());
        }
    }
}
