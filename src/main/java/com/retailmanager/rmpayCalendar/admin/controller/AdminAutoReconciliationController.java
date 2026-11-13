package com.retailmanager.rmpayCalendar.admin.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpayCalendar.admin.service.AuditService;
import com.retailmanager.rmpayCalendar.admin.service.AutoReconciliationService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Conciliación automática en el panel (Configuración): estado y cada cuántos minutos corre
 * (ver AutoReconciliationService). Exige sesión del panel como el resto de /admin/api.
 */
@RestController
@RequestMapping("/admin/api/reconciliation/auto")
public class AdminAutoReconciliationController {

    @Autowired
    private AutoReconciliationService auto;
    @Autowired
    private AuditService audit;

    @GetMapping
    public Map<String, Object> status() {
        return auto.status();
    }

    @PutMapping
    public Map<String, Object> save(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Object raw = body == null ? null : body.get("minutes");
        int minutes;
        try {
            minutes = Integer.parseInt(String.valueOf(raw).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Indique los minutos (0 = apagada)");
        }
        int before = auto.getMinutes();
        auto.setMinutes(minutes);
        if (before != minutes) {
            audit.log(AdminControllerSupport.user(request), "CONFIG_CONCILIACION_AUTO",
                    minutes == 0 ? "apagada" : "cada " + minutes + " minutos");
        }
        return auto.status();
    }
}
