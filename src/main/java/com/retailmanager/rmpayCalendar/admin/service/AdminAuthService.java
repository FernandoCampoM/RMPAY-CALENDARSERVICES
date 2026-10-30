package com.retailmanager.rmpayCalendar.admin.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Usuario del panel admin. La contraseña se guarda como hash PBKDF2 en Sys_general_config
 * (admin.passwordHash). Si no existe, al arrancar se crea admin/admin y se obliga a cambiar
 * la contraseña en el primer ingreso (el filtro bloquea la API hasta hacerlo).
 * Depende de: SyncSettingsService.get/set.
 */
@Slf4j
@Service
public class AdminAuthService {

    private static final String USERNAME = "admin.username";
    private static final String PASSWORD_HASH = "admin.passwordHash";
    private static final String MUST_CHANGE = "admin.mustChangePassword";
    /** Contraseña inicial; el panel obliga a cambiarla en el primer ingreso. */
    private static final String DEFAULT_PASSWORD = "admin";
    private static final int ITERATIONS = 120_000;
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_SECONDS = 300;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, int[]> failures = new ConcurrentHashMap<>();
    private final Map<String, Instant> lockedUntil = new ConcurrentHashMap<>();

    @Autowired
    private SyncSettingsService settings;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureAdminUser() {
        try {
            if (settings.get(PASSWORD_HASH, "").isEmpty()) {
                settings.set(USERNAME, "ADMIN USERNAME", "admin");
                settings.set(PASSWORD_HASH, "ADMIN PASSWORD HASH", hash(DEFAULT_PASSWORD));
                settings.set(MUST_CHANGE, "ADMIN MUST CHANGE PASSWORD", "true");
                log.warn("PANEL ADMIN | usuario inicial: admin / admin | se pedirá cambiar la contraseña en el primer ingreso");
            }
        } catch (Exception e) {
            log.error("ADMIN_USER_INIT_ERROR | error={}", e.getMessage());
        }
    }

    public String getUsername() {
        return settings.get(USERNAME, "admin");
    }

    public boolean mustChangePassword() {
        return Boolean.parseBoolean(settings.get(MUST_CHANGE, "false"));
    }

    /** @return mensaje de error, o null si las credenciales son válidas */
    public String authenticate(String clientKey, String username, String password) {
        Instant until = lockedUntil.get(clientKey);
        if (until != null && Instant.now().isBefore(until)) {
            return "Demasiados intentos. Espere unos minutos e intente de nuevo.";
        }
        boolean ok = username != null && password != null
                && username.equals(getUsername())
                && verify(password, settings.get(PASSWORD_HASH, ""));
        if (ok) {
            failures.remove(clientKey);
            lockedUntil.remove(clientKey);
            return null;
        }
        int count = failures.computeIfAbsent(clientKey, k -> new int[1])[0]++ + 1;
        if (count >= MAX_ATTEMPTS) {
            lockedUntil.put(clientKey, Instant.now().plusSeconds(LOCK_SECONDS));
            failures.remove(clientKey);
        }
        return "Usuario o contraseña incorrectos";
    }

    /** @return mensaje de error, o null si se cambió */
    public String changePassword(String current, String next) {
        if (current == null || !verify(current, settings.get(PASSWORD_HASH, ""))) {
            return "La contraseña actual no es correcta";
        }
        if (next == null || next.length() < 10) {
            return "La nueva contraseña debe tener al menos 10 caracteres";
        }
        if (next.equals(current)) {
            return "La nueva contraseña debe ser distinta de la actual";
        }
        settings.set(PASSWORD_HASH, "ADMIN PASSWORD HASH", hash(next));
        settings.set(MUST_CHANGE, "ADMIN MUST CHANGE PASSWORD", "false");
        return null;
    }

    private String hash(String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(pbkdf2(password, salt, ITERATIONS));
    }

    private boolean verify(String password, String stored) {
        try {
            String[] parts = stored.split("\\$");
            if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, pbkdf2(password, salt, Integer.parseInt(parts[1])));
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] pbkdf2(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }
}
