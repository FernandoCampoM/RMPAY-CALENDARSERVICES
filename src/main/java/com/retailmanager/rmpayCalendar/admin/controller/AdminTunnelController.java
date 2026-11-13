package com.retailmanager.rmpayCalendar.admin.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpayCalendar.admin.service.AdminJobRunner;
import com.retailmanager.rmpayCalendar.admin.service.AuditService;
import com.retailmanager.rmpayCalendar.admin.service.TunnelService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Túnel de Cloudflare desde el panel (ver TunnelService): estado, instalar con el token de
 * Cloudflare (tarea en segundo plano de AdminJobRunner) y desinstalar.
 * Exige sesión del panel como el resto de /admin/api. El token nunca se audita ni se devuelve.
 */
@RestController
@RequestMapping("/admin/api")
public class AdminTunnelController {

    @Autowired
    private TunnelService tunnel;
    @Autowired
    private AdminJobRunner jobs;
    @Autowired
    private AuditService audit;

    @Value("${server.port:9192}")
    private int apiPort;

    @GetMapping("/tunnel")
    public Map<String, Object> status() {
        Map<String, Object> map = tunnel.status();
        map.put("apiPort", apiPort);
        return map;
    }

    @PostMapping("/tunnel/install")
    public Map<String, Object> install(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String token = body == null || body.get("token") == null ? "" : String.valueOf(body.get("token"));
        TunnelService.TokenInfo info = TunnelService.parseToken(token);
        String reason = tunnel.unavailableReason();
        if (reason != null) {
            throw new IllegalStateException(reason);
        }
        String user = AdminControllerSupport.user(request);
        audit.log(user, "TUNEL_INSTALAR", "túnel " + info.tunnelId());
        return jobs.submit("Instalar túnel", user, job -> {
            try {
                tunnel.install(token, job);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }).toMap();
    }

    @PostMapping("/tunnel/uninstall")
    public Map<String, Object> uninstall(HttpServletRequest request) throws Exception {
        audit.log(AdminControllerSupport.user(request), "TUNEL_DESINSTALAR", TunnelService.SERVICE_ID);
        Map<String, Object> map = tunnel.uninstall();
        map.put("apiPort", apiPort);
        return map;
    }
}
