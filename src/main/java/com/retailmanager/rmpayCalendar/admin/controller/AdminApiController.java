package com.retailmanager.rmpayCalendar.admin.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailmanager.rmpayCalendar.admin.service.AdminJobRunner;
import com.retailmanager.rmpayCalendar.admin.service.AuditService;
import com.retailmanager.rmpayCalendar.admin.service.ReconciliationService;
import com.retailmanager.rmpayCalendar.admin.service.SyncSettingsService;
import com.retailmanager.rmpayCalendar.db2.entity.AdminAuditLog;
import com.retailmanager.rmpayCalendar.db2.entity.CatalogMatch;
import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProcessedOrder;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.db2.repository.AdminAuditLogRepository;
import com.retailmanager.rmpayCalendar.db2.repository.CatalogMatchRepository;
import com.retailmanager.rmpayCalendar.db2.repository.ProcessedOrderAdminRepository;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncAdminRepository;
import com.retailmanager.rmpayCalendar.db2.repository.Sys_general_configRepository;
import com.retailmanager.rmpayCalendar.enums.MatchStatus;
import com.retailmanager.rmpayCalendar.enums.SyncMaster;

import jakarta.servlet.http.HttpServletRequest;

/**
 * API del panel admin: panel, configuración, conciliación, vínculos, pedidos y auditoría.
 * Todas las rutas exigen sesión (ver AdminSecurityFilters).
 */
@RestController
@RequestMapping("/admin/api")
public class AdminApiController {

    private static final List<MatchStatus> PENDING = List.of(MatchStatus.AUTO, MatchStatus.SUGERIDO,
            MatchStatus.AMBIGUO, MatchStatus.SIN_MATCH, MatchStatus.SOLO_RM, MatchStatus.ERROR);

    @Autowired
    private SyncSettingsService settings;
    @Autowired
    private ReconciliationService reconciliation;
    @Autowired
    private AdminJobRunner jobs;
    @Autowired
    private AuditService audit;
    @Autowired
    private CatalogMatchRepository matchRepository;
    @Autowired
    private ProductSyncAdminRepository productSyncRepository;
    @Autowired
    private ProcessedOrderAdminRepository orderRepository;
    @Autowired
    private AdminAuditLogRepository auditRepository;
    @Autowired
    private Sys_general_configRepository configRepository;

    // ───────────── Panel ─────────────

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("settings", settings.asMap());
        map.put("counts", reconciliation.counts());
        map.put("linkedProducts", productSyncRepository.countByDeletedFalse());
        map.put("processedOrders", orderRepository.count());
        map.put("lastOrderSync", configRepository.getLastSyncDate());
        map.put("lastAnalysis", reconciliation.getLastAnalysis() == null ? null : reconciliation.getLastAnalysis().toString());
        map.put("job", jobs.getCurrent() == null ? null : jobs.getCurrent().toMap());
        map.put("recentAudit", auditRepository.findAllByOrderByAtDesc(PageRequest.of(0, 8)).getContent().stream()
                .map(this::auditToMap).collect(Collectors.toList()));
        return map;
    }

    // ───────────── Configuración ─────────────

    @GetMapping("/settings")
    public Map<String, Object> getSettings() {
        return settings.asMap();
    }

    @PutMapping("/settings")
    public Map<String, Object> saveSettings(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String user = AdminControllerSupport.user(request);
        Map<String, Object> before = settings.asMap();

        if (body.containsKey("master")) {
            String master = String.valueOf(body.get("master"));
            SyncMaster.valueOf(master); // valida
            settings.set(SyncSettingsService.MASTER, "SYNC MASTER", master);
        }
        if (body.containsKey("importDrafts")) {
            settings.set(SyncSettingsService.IMPORT_DRAFTS, "SYNC IMPORT DRAFTS", String.valueOf(Boolean.TRUE.equals(body.get("importDrafts"))));
        }
        if (body.containsKey("maxDeletePercent")) {
            int pct = Integer.parseInt(String.valueOf(body.get("maxDeletePercent")));
            if (pct < 0 || pct > 100) {
                throw new IllegalArgumentException("El límite de borrado debe estar entre 0 y 100");
            }
            settings.set(SyncSettingsService.MAX_DELETE_PERCENT, "SYNC MAX DELETE PERCENT", String.valueOf(pct));
        }
        if (body.containsKey("createEnabled")) {
            settings.set(SyncSettingsService.CREATE_ENABLED, "SYNC CREATE ENABLED", String.valueOf(Boolean.TRUE.equals(body.get("createEnabled"))));
        }
        Map<String, Object> after = settings.asMap();
        audit.log(user, "CONFIGURACION", "Antes: " + before + " | Después: " + after);
        return after;
    }

    // ───────────── Conciliación ─────────────

    @GetMapping("/reconciliation")
    public Map<String, Object> reconciliationPage(@RequestParam(defaultValue = "PENDIENTES") String status,
                                                  @RequestParam(required = false) String q,
                                                  @RequestParam(defaultValue = "false") boolean hideDrafts,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "50") int size) {
        List<MatchStatus> statuses = "PENDIENTES".equals(status) ? PENDING
                : "TODOS".equals(status) ? Arrays.asList(MatchStatus.values())
                : Arrays.stream(status.split(",")).map(MatchStatus::valueOf).collect(Collectors.toList());
        Page<CatalogMatch> result = matchRepository.search(statuses, AdminControllerSupport.like(q), hideDrafts,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                        Sort.by("status").and(Sort.by("shopifyTitle")).and(Sort.by("rmName"))));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("rows", result.getContent().stream().map(this::matchToMap).collect(Collectors.toList()));
        map.put("total", result.getTotalElements());
        map.put("page", result.getNumber());
        map.put("pages", result.getTotalPages());
        map.put("counts", reconciliation.counts());
        map.put("lastAnalysis", reconciliation.getLastAnalysis() == null ? null : reconciliation.getLastAnalysis().toString());
        return map;
    }

    @PostMapping("/reconciliation/analyze")
    public Map<String, Object> analyze(HttpServletRequest request) {
        String user = AdminControllerSupport.user(request);
        return jobs.submit("Analizar catálogos", user, job -> {
            job.result = reconciliation.analyze(job);
            audit.log(user, "ANALIZAR", String.valueOf(job.result));
        }).toMap();
    }

    @PostMapping("/reconciliation/link")
    public Map<String, Object> link(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String user = AdminControllerSupport.user(request);
        List<Long> ids = AdminControllerSupport.ids(body);
        return jobs.submit("Vincular", user, job -> reconciliation.link(ids, user, job)).toMap();
    }

    @PostMapping("/reconciliation/assign")
    public Map<String, Object> assign(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long id = Long.parseLong(String.valueOf(body.get("id")));
        String code = String.valueOf(body.get("rmProductCode"));
        return matchToMap(reconciliation.assign(id, code, AdminControllerSupport.user(request)));
    }

    @PostMapping("/reconciliation/ignore")
    public Map<String, Object> ignore(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        reconciliation.ignore(AdminControllerSupport.ids(body), AdminControllerSupport.user(request));
        return Map.of("success", true);
    }

    @PostMapping("/reconciliation/restore")
    public Map<String, Object> restore(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        reconciliation.restore(AdminControllerSupport.ids(body), AdminControllerSupport.user(request));
        return Map.of("success", true);
    }

    @PostMapping("/reconciliation/create-rm")
    public Map<String, Object> createRm(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String user = AdminControllerSupport.user(request);
        List<Long> ids = AdminControllerSupport.ids(body);
        return jobs.submit("Crear en RM", user, job -> reconciliation.createInRm(ids, user, job)).toMap();
    }

    @PostMapping("/reconciliation/create-shopify")
    public Map<String, Object> createShopify(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String user = AdminControllerSupport.user(request);
        List<Long> ids = AdminControllerSupport.ids(body);
        return jobs.submit("Crear en Shopify", user, job -> reconciliation.createInShopify(ids, user, job)).toMap();
    }

    @PostMapping("/reconciliation/apply-recommended")
    public Map<String, Object> applyRecommended(HttpServletRequest request) {
        String user = AdminControllerSupport.user(request);
        return jobs.submit("Aplicar acciones recomendadas", user, job -> reconciliation.applyRecommended(user, job)).toMap();
    }

    @PostMapping("/reconciliation/close")
    public Map<String, Object> close(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        boolean open = Boolean.TRUE.equals(body.get("reopen"));
        settings.set(SyncSettingsService.CREATE_ENABLED, "SYNC CREATE ENABLED", String.valueOf(!open));
        audit.log(AdminControllerSupport.user(request), open ? "REABRIR_CONCILIACION" : "CERRAR_CONCILIACION",
                open ? "Altas automáticas deshabilitadas" : "Altas automáticas habilitadas");
        return settings.asMap();
    }

    @GetMapping("/rm-products")
    public List<Map<String, Object>> rmProducts(@RequestParam(required = false) String q) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PosProduct p : reconciliation.searchRm(q, 25)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productCode", p.getProductCode());
            m.put("productName", p.getProductName());
            m.put("price", p.getPrice());
            m.put("stock", p.getCurrentStock());
            m.put("barCode", p.getBarCode());
            list.add(m);
        }
        return list;
    }

    @GetMapping("/jobs/current")
    public Map<String, Object> currentJob() {
        AdminJobRunner.Job job = jobs.getCurrent();
        Map<String, Object> map = new HashMap<>();
        map.put("job", job == null ? null : job.toMap());
        return map;
    }

    // ───────────── Vínculos, pedidos y auditoría ─────────────

    @GetMapping("/links")
    public Map<String, Object> links(@RequestParam(required = false) String q,
                                     @RequestParam(defaultValue = "false") boolean includeDeleted,
                                     @RequestParam(defaultValue = "0") int page) {
        Page<ProductSync> result = productSyncRepository.search(AdminControllerSupport.like(q), includeDeleted,
                PageRequest.of(Math.max(page, 0), 50, Sort.by(Sort.Direction.DESC, "lastSync")));
        return pageMap(result, result.getContent().stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productCode", s.getProductCode());
            m.put("shopifyProductId", s.getShopifyProductId());
            m.put("shopifyVariantId", s.getShopifyVariantId());
            m.put("pending", "PENDING".equals(s.getHash()));
            m.put("lastSync", s.getLastSync() == null ? null : s.getLastSync().toString());
            m.put("lastSeen", s.getLastSeen() == null ? null : s.getLastSeen().toString());
            m.put("deleted", Boolean.TRUE.equals(s.getDeleted()));
            return m;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/orders")
    public Map<String, Object> orders(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page) {
        Page<ProcessedOrder> result = orderRepository.search(AdminControllerSupport.like(q),
                PageRequest.of(Math.max(page, 0), 50, Sort.by(Sort.Direction.DESC, "processedAt")));
        return pageMap(result, result.getContent().stream().map(o -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("processedAt", o.getProcessedAt() == null ? null : o.getProcessedAt().toString());
            m.put("eventType", o.getEventType());
            m.put("totalAmount", o.getTotalAmount());
            return m;
        }).collect(Collectors.toList()));
    }

    @GetMapping("/audit")
    public Map<String, Object> auditLog(@RequestParam(defaultValue = "0") int page) {
        Page<AdminAuditLog> result = auditRepository.findAllByOrderByAtDesc(PageRequest.of(Math.max(page, 0), 50));
        return pageMap(result, result.getContent().stream().map(this::auditToMap).collect(Collectors.toList()));
    }

    // ───────────── Mapeos ─────────────

    private Map<String, Object> matchToMap(CatalogMatch m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("status", m.getStatus() == null ? null : m.getStatus().name());
        map.put("method", m.getMethod());
        map.put("score", m.getScore());
        map.put("shopifyProductId", m.getShopifyProductId());
        map.put("shopifyVariantId", m.getShopifyVariantId());
        map.put("shopifyHandle", m.getShopifyHandle());
        map.put("shopifyTitle", m.getShopifyTitle());
        map.put("shopifyVariantTitle", m.getShopifyVariantTitle());
        map.put("shopifyStatus", m.getShopifyStatus());
        map.put("shopifySku", m.getShopifySku());
        map.put("shopifyBarcode", m.getShopifyBarcode());
        map.put("shopifyPrice", m.getShopifyPrice());
        map.put("shopifyQty", m.getShopifyQty());
        map.put("shopifyVariantCount", m.getShopifyVariantCount());
        map.put("rmProductCode", m.getRmProductCode());
        map.put("rmName", m.getRmName());
        map.put("rmPrice", m.getRmPrice());
        map.put("rmStock", m.getRmStock());
        map.put("rmBarcode", m.getRmBarcode());
        map.put("note", m.getNote());
        map.put("updatedAt", m.getUpdatedAt() == null ? null : m.getUpdatedAt().toString());
        map.put("updatedBy", m.getUpdatedBy());
        map.put("recommendation", reconciliation.recommendation(m));
        BigDecimal sp = m.getShopifyPrice();
        BigDecimal rp = m.getRmPrice();
        map.put("priceDiff", sp != null && rp != null && sp.compareTo(rp) != 0);
        map.put("stockDiff", m.getShopifyQty() != null && m.getRmStock() != null && !m.getShopifyQty().equals(m.getRmStock()));
        return map;
    }

    private Map<String, Object> auditToMap(AdminAuditLog a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("at", a.getAt() == null ? null : a.getAt().toString());
        m.put("username", a.getUsername());
        m.put("action", a.getAction());
        m.put("detail", a.getDetail());
        return m;
    }

    private static Map<String, Object> pageMap(Page<?> page, List<Map<String, Object>> rows) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("rows", rows);
        map.put("total", page.getTotalElements());
        map.put("page", page.getNumber());
        map.put("pages", page.getTotalPages());
        return map;
    }
}
