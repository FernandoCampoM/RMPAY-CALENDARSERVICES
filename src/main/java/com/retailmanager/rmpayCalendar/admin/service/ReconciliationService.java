package com.retailmanager.rmpayCalendar.admin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpayCalendar.admin.service.ShopifyCatalogClient.ShopifyVariant;
import com.retailmanager.rmpayCalendar.db2.entity.CatalogMatch;
import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSync;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSyncEvent;
import com.retailmanager.rmpayCalendar.db2.repository.CatalogMatchRepository;
import com.retailmanager.rmpayCalendar.db2.repository.ProductSyncAdminRepository;
import com.retailmanager.rmpayCalendar.enums.MatchStatus;
import com.retailmanager.rmpayCalendar.enums.SyncAction;
import com.retailmanager.rmpayCalendar.enums.SyncMaster;
import com.retailmanager.rmpayCalendar.services.services.Shopify.ProductSyncService;
import com.retailmanager.rmpayCalendar.services.services.pos.PosClientService;
import com.retailmanager.rmpayCalendar.utils.HashUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Conciliación de catálogos Shopify ↔ Retail Manager.
 *  - analyze(): descarga ambos catálogos y clasifica cada variante (AUTO, SUGERIDO, AMBIGUO,
 *    SIN_MATCH, SOLO_RM, VINCULADO), conservando las decisiones previas (IGNORADO, CREAR_EN_RM).
 *  - link/assign/ignore/createInRm/createInShopify: acciones de la pantalla.
 *  - El vínculo definitivo se escribe en ProductSync con hash "PENDING", para que el sync
 *    envíe el stock (y precio/descripción si RM es el rey) en el siguiente ciclo.
 * Depende de: PosClientService.getAllProducts, ShopifyCatalogClient, ProductSyncService.process (alta en Shopify).
 * No modifica: PosClientService, ShopifyService, ProductSyncRepository.
 */
@Slf4j
@Service
public class ReconciliationService {

    public static final String PENDING_HASH = "PENDING";
    private static final double SUGGEST_THRESHOLD = 0.6;
    private static final Set<MatchStatus> KEEP_DECISION = EnumSet.of(MatchStatus.IGNORADO, MatchStatus.CREAR_EN_RM);

    @Autowired
    private CatalogMatchRepository matchRepository;
    @Autowired
    private ProductSyncAdminRepository productSyncRepository;
    @Autowired
    private PosClientService posClientService;
    @Autowired
    private ShopifyCatalogClient catalogClient;
    @Autowired
    private ProductSyncService productSyncService;
    @Autowired
    private PosProductWriter posProductWriter;
    @Autowired
    private SyncSettingsService settings;
    @Autowired
    private AuditService audit;

    private volatile List<PosProduct> rmCache;
    private volatile LocalDateTime lastAnalysis;

    // ───────────────────────── ANÁLISIS ─────────────────────────

    public Map<String, Object> analyze(AdminJobRunner.Job job) {
        job.message = "Leyendo productos de Retail Manager…";
        List<PosProduct> rmProducts = posClientService.getAllProducts();
        rmCache = rmProducts;

        job.message = "Leyendo catálogo de Shopify…";
        List<ShopifyVariant> variants = catalogClient.fetchAllVariants(n -> job.message = "Leyendo catálogo de Shopify… " + n + " variantes");

        job.message = "Comparando…";
        job.total = variants.size() + rmProducts.size();

        // Índice de códigos de RM
        Map<String, Set<PosProduct>> codeIndex = new HashMap<>();
        Map<String, String> codeField = new HashMap<>();
        Map<String, PosProduct> rmByCode = new HashMap<>();
        for (PosProduct p : rmProducts) {
            rmByCode.put(p.getProductCode(), p);
            index(codeIndex, codeField, p.getProductCode(), p, "CODIGO_REFERENCIA");
            index(codeIndex, codeField, p.getBarCode(), p, "CODIGO_BARCODE");
            index(codeIndex, codeField, p.getBarCode2(), p, "CODIGO_BARCODE");
        }

        // Vínculos existentes
        Map<String, ProductSync> linkedByVariant = new HashMap<>();
        Set<String> linkedCodes = new HashSet<>();
        for (ProductSync s : productSyncRepository.findAllByDeletedFalse()) {
            if (s.getShopifyVariantId() != null) {
                linkedByVariant.put(s.getShopifyVariantId(), s);
            }
            linkedCodes.add(s.getProductCode());
        }

        Map<String, CatalogMatch> existing = new HashMap<>();
        for (CatalogMatch m : matchRepository.findAll()) {
            existing.put(m.getMatchKey(), m);
        }

        Set<String> seenKeys = new HashSet<>();
        Set<String> rmCodesUsed = new HashSet<>(linkedCodes);
        List<CatalogMatch> toSave = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (ShopifyVariant v : variants) {
            String key = "S:" + v.getVariantId();
            seenKeys.add(key);
            CatalogMatch row = existing.getOrDefault(key, new CatalogMatch());
            row.setMatchKey(key);
            fillShopify(row, v);

            ProductSync link = linkedByVariant.get(v.getVariantId());
            if (link != null) {
                fillRm(row, rmByCode.get(link.getProductCode()), link.getProductCode());
                row.setStatus(MatchStatus.VINCULADO);
                if (row.getMethod() == null) {
                    row.setMethod("SYNC");
                }
                row.setNote(null);
            } else if (row.getId() != null && KEEP_DECISION.contains(row.getStatus())) {
                // Decisión previa del usuario: se conserva
                if (row.getRmProductCode() != null) {
                    rmCodesUsed.add(row.getRmProductCode());
                }
            } else {
                classify(row, v, codeIndex, codeField, rmProducts);
                if (row.getStatus() == MatchStatus.AUTO || row.getStatus() == MatchStatus.SUGERIDO) {
                    rmCodesUsed.add(row.getRmProductCode());
                }
            }
            row.setUpdatedAt(now);
            toSave.add(row);
            job.step(true, null);
        }

        // Productos solo en RM
        for (PosProduct p : rmProducts) {
            job.step(true, null);
            if (rmCodesUsed.contains(p.getProductCode())) {
                continue;
            }
            String key = "R:" + p.getProductCode();
            seenKeys.add(key);
            CatalogMatch row = existing.getOrDefault(key, new CatalogMatch());
            row.setMatchKey(key);
            fillRm(row, p, p.getProductCode());
            if (row.getId() == null || !KEEP_DECISION.contains(row.getStatus())) {
                row.setStatus(MatchStatus.SOLO_RM);
                row.setMethod(null);
                row.setScore(null);
                row.setNote(null);
            }
            row.setUpdatedAt(now);
            toSave.add(row);
        }

        // Filas que ya no existen en ningún catálogo
        List<CatalogMatch> stale = existing.values().stream()
                .filter(m -> !seenKeys.contains(m.getMatchKey()))
                .collect(Collectors.toList());
        matchRepository.deleteAll(stale);
        matchRepository.saveAll(toSave);
        lastAnalysis = now;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("shopifyVariants", variants.size());
        summary.put("rmProducts", rmProducts.size());
        summary.put("removed", stale.size());
        summary.put("counts", counts());
        return summary;
    }

    private void classify(CatalogMatch row, ShopifyVariant v, Map<String, Set<PosProduct>> codeIndex,
                          Map<String, String> codeField, List<PosProduct> rmProducts) {
        row.setNote(null);
        row.setScore(null);

        // 1) Código: SKU y código de barras contra Referencia, BarCode y BarCode2
        Set<PosProduct> candidates = new LinkedHashSet<>();
        String method = null;
        for (String code : new String[] { v.getSku(), v.getBarcode() }) {
            String n = MatchEngine.normalizeCode(code);
            if (!n.isEmpty() && codeIndex.containsKey(n)) {
                candidates.addAll(codeIndex.get(n));
                if (method == null) {
                    method = codeField.get(n);
                }
            }
        }
        if (candidates.size() == 1) {
            fillRm(row, candidates.iterator().next(), null);
            row.setStatus(MatchStatus.AUTO);
            row.setMethod(method);
            row.setScore(1.0);
            return;
        }
        if (candidates.size() > 1) {
            clearRm(row);
            row.setStatus(MatchStatus.AMBIGUO);
            row.setMethod(method);
            row.setNote("Candidatos en RM: " + candidates.stream()
                    .map(p -> p.getProductCode() + " (" + p.getProductName() + ")")
                    .collect(Collectors.joining(", ")));
            return;
        }

        // 2) Nombre: nunca se vincula solo, queda como sugerencia
        String text = shopifyText(v.getProductTitle(), v.getVariantTitle());
        PosProduct best = null;
        double bestScore = 0;
        for (PosProduct p : rmProducts) {
            double s = MatchEngine.nameScore(text, p.getProductName());
            if (s > bestScore) {
                bestScore = s;
                best = p;
            }
        }
        if (best != null && bestScore >= SUGGEST_THRESHOLD) {
            fillRm(row, best, null);
            row.setStatus(MatchStatus.SUGERIDO);
            row.setMethod("NOMBRE");
            row.setScore(bestScore);
            return;
        }

        // 3) Sin coincidencia
        clearRm(row);
        row.setStatus(MatchStatus.SIN_MATCH);
        row.setMethod(null);
        if (best != null && bestScore > 0) {
            row.setNote("Más parecido en RM: " + best.getProductCode() + " – " + best.getProductName() + " (" + bestScore + ")");
        }
    }

    // ───────────────────────── ACCIONES ─────────────────────────

    /** Filas por consulta en findRows: SQL Server admite hasta 2100 parámetros por consulta. */
    static final int ROWS_PER_QUERY = 1000;

    /** Carga filas por ID en lotes (un findAllById con miles de IDs supera el límite de SQL Server). */
    List<CatalogMatch> findRows(Collection<Long> ids) {
        List<Long> all = new ArrayList<>(ids);
        List<CatalogMatch> rows = new ArrayList<>(all.size());
        for (int from = 0; from < all.size(); from += ROWS_PER_QUERY) {
            rows.addAll(matchRepository.findAllById(all.subList(from, Math.min(from + ROWS_PER_QUERY, all.size()))));
        }
        return rows;
    }

    /** Vincula filas que ya tienen producto de RM asignado (AUTO, SUGERIDO, o asignadas a mano). */
    public void link(Collection<Long> ids, String username, AdminJobRunner.Job job) {
        List<CatalogMatch> rows = findRows(ids);
        job.total = rows.size();
        for (CatalogMatch row : rows) {
            try {
                linkRow(row, username);
                job.step(true, null);
            } catch (Exception e) {
                markError(row, username, e.getMessage());
                job.step(false, label(row) + ": " + e.getMessage());
            }
        }
        audit.log(username, "VINCULAR", job.ok + " de " + rows.size() + " filas vinculadas");
    }

    /** Asigna a mano un producto de RM a una variante de Shopify y la vincula. */
    public CatalogMatch assign(Long id, String rmProductCode, String username) {
        CatalogMatch row = matchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fila no encontrada"));
        if (row.getShopifyVariantId() == null) {
            throw new IllegalArgumentException("Solo se puede asignar a variantes de Shopify");
        }
        PosProduct product = findRm(rmProductCode);
        if (product == null) {
            throw new IllegalArgumentException("No existe el producto " + rmProductCode + " en RM (Web=True)");
        }
        fillRm(row, product, null);
        row.setMethod("MANUAL");
        row.setScore(1.0);
        linkRow(row, username);
        audit.log(username, "ASIGNAR", label(row) + " → " + rmProductCode);
        return row;
    }

    public void ignore(Collection<Long> ids, String username) {
        List<CatalogMatch> rows = findRows(ids);
        for (CatalogMatch row : rows) {
            if (row.getStatus() == MatchStatus.VINCULADO) {
                continue;
            }
            row.setStatus(MatchStatus.IGNORADO);
            touch(row, username);
        }
        matchRepository.saveAll(rows);
        audit.log(username, "IGNORAR", rows.size() + " filas");
    }

    /** Devuelve filas ignoradas o en espera a su estado calculado. */
    public void restore(Collection<Long> ids, String username) {
        List<CatalogMatch> rows = findRows(ids);
        for (CatalogMatch row : rows) {
            if (!KEEP_DECISION.contains(row.getStatus()) && row.getStatus() != MatchStatus.ERROR) {
                continue;
            }
            if (row.getShopifyVariantId() == null) {
                row.setStatus(MatchStatus.SOLO_RM);
            } else {
                row.setStatus(row.getRmProductCode() != null ? MatchStatus.SUGERIDO : MatchStatus.SIN_MATCH);
            }
            row.setNote(null);
            touch(row, username);
        }
        matchRepository.saveAll(rows);
        audit.log(username, "RESTAURAR", rows.size() + " filas");
    }

    /** Crea en RM variantes de Shopify sin contraparte (ver PosProductWriter). */
    public void createInRm(Collection<Long> ids, String username, AdminJobRunner.Job job) {
        List<CatalogMatch> rows = findRows(ids);
        job.total = rows.size();
        boolean configured = posProductWriter.isConfigured();
        for (CatalogMatch row : rows) {
            try {
                if (row.getShopifyVariantId() == null || row.getStatus() == MatchStatus.VINCULADO) {
                    throw new IllegalStateException("No es una variante de Shopify pendiente");
                }
                String code = MatchEngine.rmCode(row.getShopifySku() != null ? row.getShopifySku() : row.getShopifyBarcode());
                if (code.isEmpty()) {
                    throw new IllegalStateException("No tiene SKU ni código de barras; asígnele uno en Shopify");
                }
                if (!configured) {
                    row.setStatus(MatchStatus.CREAR_EN_RM);
                    row.setNote("En espera: falta configurar el endpoint del POS para crear productos");
                    touch(row, username);
                    matchRepository.save(row);
                    job.step(true, null);
                    continue;
                }
                String created = posProductWriter.create(row, code);
                row.setRmProductCode(created);
                row.setRmName(row.getShopifyTitle());
                row.setRmPrice(row.getShopifyPrice());
                row.setRmStock(0);
                row.setMethod("CREADO_EN_RM");
                linkRow(row, username);
                job.step(true, null);
            } catch (Exception e) {
                markError(row, username, e.getMessage());
                job.step(false, label(row) + ": " + e.getMessage());
            }
        }
        audit.log(username, "CREAR_EN_RM", job.ok + " de " + rows.size() + (configured ? " creadas" : " marcadas en espera"));
    }

    /** Crea en Shopify productos que solo existen en RM, usando el mismo flujo del sync. */
    public void createInShopify(Collection<Long> ids, String username, AdminJobRunner.Job job) {
        List<CatalogMatch> rows = findRows(ids);
        job.total = rows.size();
        for (CatalogMatch row : rows) {
            try {
                if (row.getShopifyVariantId() != null || row.getRmProductCode() == null) {
                    throw new IllegalStateException("No es un producto solo de RM");
                }
                if (productSyncRepository.findFirstByProductCodeAndDeletedFalse(row.getRmProductCode()) != null) {
                    throw new IllegalStateException("Ya está vinculado");
                }
                PosProduct product = findRm(row.getRmProductCode());
                if (product == null) {
                    throw new IllegalStateException("Ya no viene del POS con Web=True");
                }
                ProductSyncEvent event = new ProductSyncEvent();
                event.setProduct(product);
                event.setAction(SyncAction.CREATE);
                event.setHash(HashUtils.generateProductHash(product));
                productSyncService.process(event);

                ProductSync sync = productSyncRepository.findFirstByProductCodeAndDeletedFalse(product.getProductCode());
                row.setShopifyProductId(sync == null ? null : sync.getShopifyProductId());
                row.setStatus(MatchStatus.VINCULADO);
                row.setMethod("CREADO_EN_SHOPIFY");
                row.setNote(null);
                touch(row, username);
                matchRepository.save(row);
                job.step(true, null);
            } catch (Exception e) {
                markError(row, username, rootMessage(e));
                job.step(false, label(row) + ": " + rootMessage(e));
            }
        }
        audit.log(username, "CREAR_EN_SHOPIFY", job.ok + " de " + rows.size() + " creados");
    }

    /**
     * Aplica la acción recomendada a todas las filas que la tienen clara:
     * AUTO → vincular; y según el rey, crear en RM o en Shopify. SUGERIDO y AMBIGUO nunca se aplican solos.
     */
    public void applyRecommended(String username, AdminJobRunner.Job job) {
        List<CatalogMatch> rows = matchRepository.findAllByStatusIn(
                List.of(MatchStatus.AUTO, MatchStatus.SIN_MATCH, MatchStatus.SOLO_RM));
        List<Long> toLink = new ArrayList<>();
        List<Long> toRm = new ArrayList<>();
        List<Long> toShopify = new ArrayList<>();
        List<Long> toIgnore = new ArrayList<>();
        for (CatalogMatch row : rows) {
            switch (recommendation(row)) {
                case "LINK" -> toLink.add(row.getId());
                case "CREATE_RM" -> toRm.add(row.getId());
                case "CREATE_SHOPIFY" -> toShopify.add(row.getId());
                case "IGNORE" -> toIgnore.add(row.getId());
                default -> { }
            }
        }
        int total = toLink.size() + toRm.size() + toShopify.size() + toIgnore.size();
        AdminJobRunner.Job sub = new AdminJobRunner.Job("sub", username);

        job.message = "Vinculando " + toLink.size() + " coincidencias por código…";
        link(toLink, username, sub);
        job.total = total;
        job.done = sub.done;
        job.ok = sub.ok;

        if (!toIgnore.isEmpty()) {
            ignore(toIgnore, username);
            job.done += toIgnore.size();
            job.ok += toIgnore.size();
        }
        if (!toRm.isEmpty()) {
            job.message = "Creando " + toRm.size() + " productos en RM…";
            AdminJobRunner.Job rmJob = new AdminJobRunner.Job("sub", username);
            createInRm(toRm, username, rmJob);
            job.done += rmJob.done;
            job.ok += rmJob.ok;
        }
        if (!toShopify.isEmpty()) {
            job.message = "Creando " + toShopify.size() + " productos en Shopify…";
            AdminJobRunner.Job shJob = new AdminJobRunner.Job("sub", username);
            createInShopify(toShopify, username, shJob);
            job.done += shJob.done;
            job.ok += shJob.ok;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("vinculadas", toLink.size());
        result.put("crearEnRm", toRm.size());
        result.put("crearEnShopify", toShopify.size());
        result.put("ignoradas", toIgnore.size());
        job.result = result;
        audit.log(username, "APLICAR_RECOMENDADAS", result.toString());
    }

    /** Código de la acción sugerida para la fila según su estado y el rey configurado. */
    public String recommendation(CatalogMatch row) {
        SyncMaster master = settings.getMaster();
        if (row.getStatus() == null) {
            return "NONE";
        }
        return switch (row.getStatus()) {
            case AUTO -> "LINK";
            case SUGERIDO -> "REVIEW";
            case AMBIGUO -> "CHOOSE";
            case SIN_MATCH -> {
                if (master != SyncMaster.SHOPIFY) {
                    yield "ASSIGN";
                }
                yield "DRAFT".equalsIgnoreCase(row.getShopifyStatus()) && !settings.isImportDrafts() ? "IGNORE" : "CREATE_RM";
            }
            case SOLO_RM -> master == SyncMaster.RM ? "CREATE_SHOPIFY" : "REVIEW_RM";
            default -> "NONE";
        };
    }

    // ───────────────────────── CONSULTAS ─────────────────────────

    public Map<String, Long> counts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (MatchStatus s : MatchStatus.values()) {
            counts.put(s.name(), 0L);
        }
        for (Object[] r : matchRepository.countByStatus()) {
            if (r[0] != null) {
                counts.put(((MatchStatus) r[0]).name(), (Long) r[1]);
            }
        }
        return counts;
    }

    public LocalDateTime getLastAnalysis() {
        return lastAnalysis;
    }

    /** Busca productos de RM para el diálogo de asignación manual. */
    public List<PosProduct> searchRm(String q, int limit) {
        List<PosProduct> products = rmProducts();
        String needle = q == null ? "" : q.trim().toUpperCase(Locale.ROOT);
        String code = MatchEngine.normalizeCode(q);
        return products.stream()
                .filter(p -> needle.isEmpty()
                        || (p.getProductName() != null && p.getProductName().toUpperCase(Locale.ROOT).contains(needle))
                        || MatchEngine.normalizeCode(p.getProductCode()).contains(code)
                        || MatchEngine.normalizeCode(p.getBarCode()).contains(code))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ───────────────────────── AUXILIARES ─────────────────────────

    private void linkRow(CatalogMatch row, String username) {
        if (row.getShopifyVariantId() == null || row.getRmProductCode() == null) {
            throw new IllegalStateException("La fila no tiene producto de Shopify y de RM");
        }
        ProductSync byCode = productSyncRepository.findFirstByProductCodeAndDeletedFalse(row.getRmProductCode());
        ProductSync byVariant = productSyncRepository.findFirstByShopifyVariantIdAndDeletedFalse(row.getShopifyVariantId());
        if (byVariant != null && !row.getRmProductCode().equals(byVariant.getProductCode())) {
            throw new IllegalStateException("La variante ya está vinculada a " + byVariant.getProductCode());
        }
        if (byCode != null && !row.getShopifyVariantId().equals(byCode.getShopifyVariantId())) {
            throw new IllegalStateException("El producto " + row.getRmProductCode() + " de RM ya está vinculado a otra variante");
        }
        if (byCode == null) {
            ProductSync sync = new ProductSync();
            sync.setProductCode(row.getRmProductCode());
            sync.setShopifyProductId(row.getShopifyProductId());
            sync.setShopifyVariantId(row.getShopifyVariantId());
            sync.setShopifyInventoryItemId(row.getShopifyInventoryItemId());
            sync.setHash(PENDING_HASH);
            sync.setLastSync(LocalDateTime.now());
            sync.setLastSeen(LocalDateTime.now());
            sync.setDeleted(false);
            productSyncRepository.save(sync);
        }
        row.setStatus(MatchStatus.VINCULADO);
        row.setNote(null);
        touch(row, username);
        matchRepository.save(row);

        // El producto deja de estar "solo en RM"
        CatalogMatch rmOnly = matchRepository.findByMatchKey("R:" + row.getRmProductCode());
        if (rmOnly != null) {
            matchRepository.delete(rmOnly);
        }
    }

    private void markError(CatalogMatch row, String username, String message) {
        row.setStatus(MatchStatus.ERROR);
        row.setNote(message == null ? "Error desconocido" : message.length() > 990 ? message.substring(0, 990) : message);
        touch(row, username);
        matchRepository.save(row);
    }

    private PosProduct findRm(String code) {
        String n = MatchEngine.normalizeCode(code);
        return rmProducts().stream()
                .filter(p -> MatchEngine.normalizeCode(p.getProductCode()).equals(n))
                .findFirst().orElse(null);
    }

    private List<PosProduct> rmProducts() {
        List<PosProduct> cache = rmCache;
        if (cache == null) {
            cache = posClientService.getAllProducts();
            rmCache = cache;
        }
        return cache;
    }

    private static void index(Map<String, Set<PosProduct>> codeIndex, Map<String, String> codeField,
                              String code, PosProduct p, String field) {
        String n = MatchEngine.normalizeCode(code);
        if (n.isEmpty()) {
            return;
        }
        codeIndex.computeIfAbsent(n, k -> new LinkedHashSet<>()).add(p);
        codeField.putIfAbsent(n, field);
    }

    private static void fillShopify(CatalogMatch row, ShopifyVariant v) {
        row.setShopifyProductId(v.getProductId());
        row.setShopifyVariantId(v.getVariantId());
        row.setShopifyInventoryItemId(v.getInventoryItemId());
        row.setShopifyHandle(v.getHandle());
        row.setShopifyTitle(v.getProductTitle());
        row.setShopifyVariantTitle("Default Title".equals(v.getVariantTitle()) ? null : v.getVariantTitle());
        row.setShopifyStatus(v.getStatus());
        row.setShopifySku(v.getSku());
        row.setShopifyBarcode(v.getBarcode());
        row.setShopifyPrice(v.getPrice());
        row.setShopifyQty(v.getQuantity());
        row.setShopifyVariantCount(v.getVariantCount());
    }

    private static void fillRm(CatalogMatch row, PosProduct p, String fallbackCode) {
        if (p == null) {
            row.setRmProductCode(fallbackCode);
            return;
        }
        row.setRmProductCode(p.getProductCode());
        row.setRmName(p.getProductName());
        row.setRmPrice(p.getPrice());
        row.setRmStock(p.getCurrentStock());
        row.setRmBarcode(p.getBarCode());
    }

    private static void clearRm(CatalogMatch row) {
        row.setRmProductCode(null);
        row.setRmName(null);
        row.setRmPrice(null);
        row.setRmStock(null);
        row.setRmBarcode(null);
    }

    private static void touch(CatalogMatch row, String username) {
        row.setUpdatedAt(LocalDateTime.now());
        row.setUpdatedBy(username);
    }

    private static String shopifyText(String title, String variantTitle) {
        return variantTitle == null || "Default Title".equals(variantTitle) ? title : title + " " + variantTitle;
    }

    private static String label(CatalogMatch row) {
        return row.getShopifyTitle() != null ? row.getShopifyTitle() : row.getRmName() != null ? row.getRmName() : String.valueOf(row.getId());
    }

    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage() != null ? t.getMessage() : e.getMessage();
    }
}
