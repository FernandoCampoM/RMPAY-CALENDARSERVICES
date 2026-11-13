package com.retailmanager.rmpayCalendar.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.retailmanager.rmpayCalendar.admin.config.AdminSecurityFilters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilidades comunes de los controladores del panel y manejo de errores en formato JSON.
 * Solo aplica a los controladores del paquete admin.controller.
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.retailmanager.rmpayCalendar.admin.controller")
public class AdminControllerSupport {

    static String user(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute(AdminSecurityFilters.SESSION_USER);
        return user == null ? "desconocido" : user.toString();
    }

    static List<Long> ids(Map<String, Object> body) {
        Object raw = body == null ? null : body.get("ids");
        List<Long> ids = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Number n) {
                    ids.add(n.longValue());
                } else if (o != null) {
                    ids.add(Long.parseLong(o.toString()));
                }
            }
        }
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("No se seleccionó ninguna fila");
        }
        return ids;
    }

    /** Convierte texto de búsqueda en patrón LIKE en minúsculas, o null si está vacío. */
    static String like(String q) {
        return q == null || q.isBlank() ? null : "%" + q.trim().toLowerCase() + "%";
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<?> badRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "error", String.valueOf(e.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception e) {
        log.error("ADMIN_API_ERROR | error={}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Error interno: " + e.getMessage()));
    }
}
