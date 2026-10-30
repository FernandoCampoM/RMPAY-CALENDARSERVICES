package com.retailmanager.rmpayCalendar.admin.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpayCalendar.admin.config.AdminSecurityFilters;
import com.retailmanager.rmpayCalendar.admin.service.AdminAuthService;
import com.retailmanager.rmpayCalendar.admin.service.AuditService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Login, logout y cambio de contraseña del panel admin (sesión HTTP).
 */
@RestController
@RequestMapping("/admin/api/auth")
public class AdminAuthController {

    @Autowired
    private AdminAuthService authService;
    @Autowired
    private AuditService audit;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String error = authService.authenticate(request.getRemoteAddr(), username, body.get("password"));
        if (error != null) {
            audit.log(username, "LOGIN_FALLIDO", "IP " + request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", error));
        }
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(8 * 60 * 60);
        session.setAttribute(AdminSecurityFilters.SESSION_USER, username);
        if (authService.mustChangePassword()) {
            session.setAttribute(AdminSecurityFilters.SESSION_MUST_CHANGE, true);
        }
        audit.log(username, "LOGIN", "IP " + request.getRemoteAddr());
        return ResponseEntity.ok(me(username));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(HttpServletRequest request) {
        return ResponseEntity.ok(me(AdminControllerSupport.user(request)));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String user = AdminControllerSupport.user(request);
        String error = authService.changePassword(body.get("current"), body.get("next"));
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", error));
        }
        request.getSession().removeAttribute(AdminSecurityFilters.SESSION_MUST_CHANGE);
        audit.log(user, "CAMBIAR_CONTRASENA", "");
        return ResponseEntity.ok(Map.of("success", true));
    }

    private Map<String, Object> me(String username) {
        return Map.of("success", true, "username", username, "mustChangePassword", authService.mustChangePassword());
    }
}
