package com.retailmanager.rmpayCalendar.admin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Ejecuta en segundo plano, de a una, las tareas largas del panel (análisis de catálogos,
 * acciones en bloque) y expone su progreso para que la pantalla lo consulte.
 */
@Slf4j
@Component
public class AdminJobRunner {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "admin-job");
        t.setDaemon(true);
        return t;
    });

    private volatile Job current;

    public synchronized Job submit(String name, String username, Consumer<Job> work) {
        if (current != null && current.running) {
            throw new IllegalStateException("Ya hay una tarea en curso: " + current.name);
        }
        Job job = new Job(name, username);
        current = job;
        executor.submit(() -> {
            try {
                work.accept(job);
                job.message = job.errors.isEmpty() ? "Terminado" : "Terminado con errores";
            } catch (Exception e) {
                log.error("ADMIN_JOB_ERROR | job={} | error={}", name, e.getMessage(), e);
                job.message = "Falló: " + e.getMessage();
                job.failed = true;
            } finally {
                job.finishedAt = LocalDateTime.now();
                job.running = false;
            }
        });
        return job;
    }

    public Job getCurrent() {
        return current;
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    public static class Job {
        public final String name;
        public final String username;
        public final LocalDateTime startedAt = LocalDateTime.now();
        public volatile LocalDateTime finishedAt;
        public volatile boolean running = true;
        public volatile boolean failed;
        public volatile int total;
        public volatile int done;
        public volatile int ok;
        public volatile String message = "En curso";
        public volatile Map<String, Object> result = Collections.emptyMap();
        private final List<String> errors = Collections.synchronizedList(new ArrayList<>());

        Job(String name, String username) {
            this.name = name;
            this.username = username;
        }

        public void step(boolean success, String error) {
            done++;
            if (success) {
                ok++;
            } else if (error != null && errors.size() < 200) {
                errors.add(error);
            }
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", name);
            map.put("username", username);
            map.put("startedAt", startedAt.toString());
            map.put("finishedAt", finishedAt == null ? null : finishedAt.toString());
            map.put("running", running);
            map.put("failed", failed);
            map.put("total", total);
            map.put("done", done);
            map.put("ok", ok);
            map.put("message", message);
            map.put("errors", new ArrayList<>(errors));
            map.put("result", result);
            return map;
        }
    }
}
