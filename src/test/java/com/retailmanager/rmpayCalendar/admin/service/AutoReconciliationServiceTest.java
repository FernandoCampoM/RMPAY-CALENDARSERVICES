package com.retailmanager.rmpayCalendar.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Conciliación automática: corre "analizar + aplicar recomendadas" cada N minutos desde el último
 * análisis, se apaga con 0 y salta el turno si ya hay otra tarea del panel en curso.
 */
class AutoReconciliationServiceTest {

    private AutoReconciliationService auto;
    private SyncSettingsService settings;
    private ReconciliationService reconciliation;
    private AdminJobRunner jobs;

    @BeforeEach
    void setUp() {
        auto = new AutoReconciliationService();
        settings = mock(SyncSettingsService.class);
        reconciliation = mock(ReconciliationService.class);
        jobs = new AdminJobRunner();
        ReflectionTestUtils.setField(auto, "settings", settings);
        ReflectionTestUtils.setField(auto, "reconciliation", reconciliation);
        ReflectionTestUtils.setField(auto, "jobs", jobs);
        ReflectionTestUtils.setField(auto, "audit", mock(AuditService.class));
        when(reconciliation.analyze(any())).thenReturn(Map.of("total", 3));
    }

    private void minutes(String value) {
        when(settings.get(eq(AutoReconciliationService.MINUTES), anyString())).thenReturn(value);
    }

    private void nowIs(LocalDateTime now) {
        ReflectionTestUtils.setField(auto, "clock", Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    }

    @Test
    void porDefectoCada15Minutos() {
        when(settings.get(eq(AutoReconciliationService.MINUTES), anyString())).thenAnswer(i -> i.getArgument(1));
        assertEquals(15, auto.getMinutes());
    }

    @Test
    void cuandoTocaAnalizaYAplicaRecomendadas() {
        minutes("15");
        LocalDateTime last = LocalDateTime.now().plusSeconds(1); // posterior al arranque del servicio
        when(reconciliation.getLastAnalysis()).thenReturn(last);
        nowIs(last.plusMinutes(16));

        auto.tick();

        verify(reconciliation, timeout(5000)).analyze(any());
        verify(reconciliation, timeout(5000)).applyRecommended(eq("automático"), any());
    }

    @Test
    void antesDeTiempoNoHaceNada() {
        minutes("15");
        LocalDateTime last = LocalDateTime.now().plusMinutes(1);
        when(reconciliation.getLastAnalysis()).thenReturn(last);
        nowIs(last.plusMinutes(10));

        auto.tick();

        verify(reconciliation, never()).analyze(any());
    }

    @Test
    void conCeroQuedaApagada() {
        minutes("0");
        when(reconciliation.getLastAnalysis()).thenReturn(LocalDateTime.now().minusDays(1));

        auto.tick();

        assertNull(auto.nextRun());
        assertFalse((Boolean) auto.status().get("enabled"));
        verify(reconciliation, never()).analyze(any());
    }

    @Test
    void siHayOtraTareaEnCursoSaltaElTurno() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        jobs.submit("Crear en RM", "admin", job -> {
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        try {
            assertFalse(auto.runNow());
            verify(reconciliation, never()).analyze(any());
        } finally {
            release.countDown();
        }
    }

    @Test
    void validaLosMinutos() {
        assertThrows(IllegalArgumentException.class, () -> auto.setMinutes(-1));
        assertThrows(IllegalArgumentException.class, () -> auto.setMinutes(1441));
        auto.setMinutes(30);
        verify(settings).set(eq(AutoReconciliationService.MINUTES), anyString(), eq("30"));
        assertTrue(true);
    }
}
