package com.retailmanager.rmpayCalendar.admin.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Conciliación automática: cada N minutos (sync.autoReconcileMinutes, editable en el panel →
 * Configuración; 0 = apagada) hace lo mismo que los botones "Analizar catálogos" y "Aplicar
 * acciones recomendadas": vincula por código y crea en RM o en Shopify según quién manda.
 * Sugeridos y ambiguos siguen esperando revisión manual.
 * Corre como una tarea más de AdminJobRunner: si hay otra en curso (por ejemplo una manual), ese
 * turno se salta. Revisa cada minuto si ya toca; el intervalo cuenta desde el último análisis,
 * sea automático o manual.
 */
@Slf4j
@Service
public class AutoReconciliationService {

    public static final String MINUTES = "sync.autoReconcileMinutes";
    static final int DEFAULT_MINUTES = 15;
    static final String JOB_NAME = "Conciliación automática";
    static final String USER = "automático";

    @Autowired
    private SyncSettingsService settings;
    @Autowired
    private ReconciliationService reconciliation;
    @Autowired
    private AdminJobRunner jobs;
    @Autowired
    private AuditService audit;

    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "conciliacion-automatica");
        t.setDaemon(true);
        return t;
    });
    private Clock clock = Clock.systemDefaultZone();
    private final LocalDateTime startedAt = LocalDateTime.now();
    private volatile LocalDateTime lastRun;
    private volatile String lastResult;

    @PostConstruct
    void start() {
        timer.scheduleWithFixedDelay(this::tick, 1, 1, TimeUnit.MINUTES);
    }

    @PreDestroy
    void stop() {
        timer.shutdownNow();
    }

    /** Minutos entre ciclos; 0 = apagada. */
    public int getMinutes() {
        try {
            return Math.max(0, Integer.parseInt(settings.get(MINUTES, String.valueOf(DEFAULT_MINUTES)).trim()));
        } catch (NumberFormatException e) {
            return DEFAULT_MINUTES;
        }
    }

    public void setMinutes(int minutes) {
        if (minutes < 0 || minutes > 1440) {
            throw new IllegalArgumentException("Los minutos deben estar entre 0 (apagada) y 1440");
        }
        settings.set(MINUTES, "SYNC AUTO RECONCILE MINUTES", String.valueOf(minutes));
    }

    /**
     * Desde cuándo se cuenta el intervalo: lo más reciente entre el último análisis (automático o
     * manual), el último ciclo automático (aunque haya fallado, para no reintentar cada minuto) y
     * el arranque del servicio.
     */
    LocalDateTime lastAnalysisOrStart() {
        LocalDateTime base = startedAt;
        LocalDateTime last = reconciliation.getLastAnalysis();
        if (last != null && last.isAfter(base)) {
            base = last;
        }
        if (lastRun != null && lastRun.isAfter(base)) {
            base = lastRun;
        }
        return base;
    }

    /** Próximo ciclo, o null si está apagada. */
    public LocalDateTime nextRun() {
        int minutes = getMinutes();
        return minutes == 0 ? null : lastAnalysisOrStart().plusMinutes(minutes);
    }

    /** Se llama cada minuto: lanza el ciclo si ya toca. */
    void tick() {
        try {
            LocalDateTime next = nextRun();
            if (next != null && !LocalDateTime.now(clock).isBefore(next)) {
                runNow();
            }
        } catch (Exception e) {
            log.error("AUTO_RECONCILE_ERROR | error={}", e.getMessage(), e);
        }
    }

    /** Analiza y aplica las acciones recomendadas como una tarea de AdminJobRunner. @return false si había otra tarea en curso. */
    boolean runNow() {
        try {
            jobs.submit(JOB_NAME, USER, job -> {
                lastRun = LocalDateTime.now(clock);
                job.message = "Analizando catálogos";
                Map<String, Object> analysis = reconciliation.analyze(job);
                job.message = "Aplicando acciones recomendadas";
                AdminJobRunner.Job apply = new AdminJobRunner.Job(JOB_NAME, USER);
                reconciliation.applyRecommended(USER, apply);
                lastResult = "Análisis: " + analysis + " | Aplicado: " + apply.result;
                job.result = apply.result;
                audit.log(USER, "CONCILIACION_AUTOMATICA", lastResult);
                log.info("AUTO_RECONCILE | {}", lastResult);
            });
            return true;
        } catch (IllegalStateException busy) {
            log.info("AUTO_RECONCILE_SKIPPED | {}", busy.getMessage());
            return false;
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> map = new LinkedHashMap<>();
        int minutes = getMinutes();
        map.put("minutes", minutes);
        map.put("enabled", minutes > 0);
        map.put("lastRun", lastRun == null ? null : lastRun.toString());
        map.put("lastResult", lastResult);
        LocalDateTime last = reconciliation.getLastAnalysis();
        map.put("lastAnalysis", last == null ? null : last.toString());
        LocalDateTime next = nextRun();
        map.put("nextRun", next == null ? null : next.toString());
        map.put("minutesToNext", next == null ? null : Math.max(0, Duration.between(LocalDateTime.now(clock), next).toMinutes()));
        return map;
    }
}
