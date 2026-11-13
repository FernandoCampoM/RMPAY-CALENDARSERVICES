/* Panel admin RM ↔ Shopify. JavaScript sin dependencias externas. */
(() => {
  'use strict';

  // ───────── Utilidades ─────────
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));
  const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const num = (v) => (v == null ? '—' : Number(v).toLocaleString('es'));
  const money = (v) => (v == null ? '—' : '$' + Number(v).toLocaleString('es', { minimumFractionDigits: 2, maximumFractionDigits: 2 }));
  const date = (v) => {
    if (!v) return '—';
    const d = new Date(v);
    return isNaN(d) ? esc(v) : d.toLocaleString('es', { dateStyle: 'short', timeStyle: 'short' });
  };
  const cleanCode = (v) => (v ? String(v).replace(/^'+/, '') : '');
  const debounce = (fn, ms = 300) => { let t; return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); }; };
  const icon = (path) => `<svg viewBox="0 0 24 24">${path}</svg>`;
  const ICONS = {
    warn: icon('<path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0zM12 9v4M12 17h.01"/>'),
    ok: icon('<path d="M22 11.1V12a10 10 0 1 1-5.9-9.1"/><path d="M22 4 12 14l-3-3"/>'),
    search: icon('<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/>'),
    refresh: icon('<path d="M21 12a9 9 0 1 1-2.6-6.4L21 8"/><path d="M21 3v5h-5"/>'),
    arrow: icon('<path d="M5 12h14M13 6l6 6-6 6"/>'),
    wand: icon('<path d="m15 4 5 5L9 20H4v-5zM13 6l5 5"/>'),
    lock: icon('<rect x="4" y="11" width="16" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 8 0v4"/>'),
    unlock: icon('<rect x="4" y="11" width="16" height="10" rx="2"/><path d="M8 11V7a4 4 0 0 1 7.9-1"/>'),
    sun: icon('<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>'),
    moon: icon('<path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"/>'),
    auto: icon('<rect x="3" y="4" width="18" height="16" rx="2"/><path d="M3 10h18"/>'),
  };

  const STATUS = {
    AUTO: ['Por código', 'badge-ok'],
    SUGERIDO: ['Sugerido', 'badge-info'],
    AMBIGUO: ['Ambiguo', 'badge-warn'],
    SIN_MATCH: ['Sin coincidencia', 'badge-warn'],
    SOLO_RM: ['Solo en RM', 'badge-accent'],
    VINCULADO: ['Vinculado', 'badge-ok'],
    IGNORADO: ['Ignorado', ''],
    CREAR_EN_RM: ['Alta en RM pendiente', 'badge-info'],
    ERROR: ['Error', 'badge-danger'],
  };
  const TABS = [
    ['PENDIENTES', 'Por revisar'], ['AUTO', 'Por código'], ['SUGERIDO', 'Sugeridos'], ['AMBIGUO', 'Ambiguos'],
    ['SIN_MATCH', 'Sin coincidencia'], ['SOLO_RM', 'Solo en RM'], ['ERROR', 'Con error'],
    ['CREAR_EN_RM', 'Alta en RM pendiente'], ['VINCULADO', 'Vinculados'], ['IGNORADO', 'Ignorados'], ['TODOS', 'Todos'],
  ];
  const PENDING_KEYS = ['AUTO', 'SUGERIDO', 'AMBIGUO', 'SIN_MATCH', 'SOLO_RM', 'ERROR'];
  const MASTER = {
    RM: { label: 'Retail Manager', short: 'RM' },
    SHOPIFY: { label: 'Shopify', short: 'Shopify' },
    NINGUNO: { label: 'Ninguno', short: 'Ninguno' },
  };
  const METHOD = { CODIGO_REFERENCIA: 'Referencia', CODIGO_BARCODE: 'Código de barras', NOMBRE: 'Nombre', MANUAL: 'Manual', SYNC: 'Sync', CREADO_EN_RM: 'Creado en RM', CREADO_EN_SHOPIFY: 'Creado en Shopify' };
  const badge = (status) => { const [l, c] = STATUS[status] || [status, '']; return `<span class="badge ${c}">${esc(l)}</span>`; };

  // ───────── Estado ─────────
  const state = {
    user: null,
    settings: null,
    recon: { status: 'PENDIENTES', q: '', hideDrafts: false, page: 0, selected: new Set(), rows: [] },
    links: { q: '', includeDeleted: false, page: 0 },
    orders: { q: '', page: 0 },
    failed: { q: '', page: 0 },
    webhooks: { q: '', page: 0 },
    audit: { page: 0 },
    jobTimer: null,
    lastJobKey: null,
  };

  // ───────── API ─────────
  async function api(path, { method = 'GET', body } = {}) {
    const res = await fetch('/admin/api' + path, {
      method,
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json', 'X-Admin-Request': '1' },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    let data = null;
    try { data = await res.json(); } catch (e) { /* sin cuerpo */ }
    if (res.status === 401 && !path.startsWith('/auth/login')) { showLogin(); throw new Error('Sesión expirada'); }
    if (res.status === 403 && data && /contraseña/i.test(data.error || '')) { showForcePassword(); throw new Error(data.error); }
    if (!res.ok) throw new Error((data && data.error) || `Error ${res.status}`);
    return data;
  }

  // ───────── Toasts y confirmación ─────────
  function toast(msg, type = '') {
    const el = document.createElement('div');
    el.className = 'toast' + (type === 'error' ? ' toast-error' : '');
    el.textContent = msg;
    $('#toasts').appendChild(el);
    setTimeout(() => el.remove(), type === 'error' ? 6000 : 3500);
  }
  function confirmDialog(title, text, okLabel = 'Confirmar') {
    const dlg = $('#confirm-dialog');
    $('#confirm-title').textContent = title;
    $('#confirm-text').textContent = text;
    $('#confirm-ok').textContent = okLabel;
    return new Promise((resolve) => {
      dlg.addEventListener('close', () => resolve(dlg.returnValue === 'ok'), { once: true });
      dlg.showModal();
    });
  }

  // ───────── Tema ─────────
  function currentTheme() {
    const forced = document.documentElement.dataset.theme;
    if (forced) return forced;
    return matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  function paintThemeButtons() {
    const dark = currentTheme() === 'dark';
    $$('.theme-toggle').forEach((b) => {
      b.innerHTML = (dark ? ICONS.sun : ICONS.moon) + `<span class="theme-label">${dark ? 'Modo claro' : 'Modo oscuro'}</span>`;
      b.setAttribute('aria-label', dark ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro');
    });
  }
  function toggleTheme() {
    const next = currentTheme() === 'dark' ? 'light' : 'dark';
    document.documentElement.dataset.theme = next;
    try { localStorage.setItem('rm-admin-theme', next); } catch (e) { /* almacenamiento no disponible */ }
    paintThemeButtons();
  }

  // ───────── Autenticación ─────────
  function showLogin() {
    stopJobPolling();
    $('#app').hidden = true;
    $('#force-password-view').hidden = true;
    $('#login-view').hidden = false;
    setTimeout(() => $('#login-form [name=username]').focus(), 0);
  }
  function showForcePassword() {
    $('#app').hidden = true;
    $('#login-view').hidden = true;
    $('#force-password-view').hidden = false;
  }
  function showApp() {
    $('#login-view').hidden = true;
    $('#force-password-view').hidden = true;
    $('#app').hidden = false;
    $('#user-name').textContent = state.user;
    $('#user-avatar').textContent = (state.user || '?').slice(0, 2);
    route();
    checkJob();
  }

  $('#login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = e.target;
    $('#login-error').textContent = '';
    try {
      const me = await api('/auth/login', { method: 'POST', body: { username: f.username.value.trim(), password: f.password.value } });
      f.password.value = '';
      state.user = me.username;
      me.mustChangePassword ? showForcePassword() : showApp();
    } catch (err) {
      $('#login-error').textContent = err.message;
    }
  });

  $('#force-password-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = e.target;
    const errEl = $('.form-error', f);
    errEl.textContent = '';
    if (f.next.value !== f.confirm.value) { errEl.textContent = 'Las contraseñas nuevas no coinciden'; return; }
    try {
      await api('/auth/change-password', { method: 'POST', body: { current: f.current.value, next: f.next.value } });
      f.reset();
      toast('Contraseña actualizada');
      showApp();
    } catch (err) {
      errEl.textContent = err.message;
    }
  });

  $('#logout-btn').addEventListener('click', async () => {
    try { await api('/auth/logout', { method: 'POST' }); } catch (e) { /* igual se sale */ }
    state.user = null;
    showLogin();
  });

  // ───────── Navegación ─────────
  const VIEWS = {
    panel: ['Panel', renderPanel],
    conciliacion: ['Conciliación', renderRecon],
    configuracion: ['Configuración', renderSettings],
    vinculados: ['Productos vinculados', renderLinks],
    pedidos: ['Pedidos procesados', renderOrders],
    'facturas-error': ['Facturas con error', renderFailedOrders],
    webhooks: ['Webhooks recibidos', renderWebhooks],
    tunel: ['Túnel de Cloudflare', renderTunnel],
    guias: ['Guías', renderGuides],
    auditoria: ['Auditoría', renderAudit],
  };
  function currentView() {
    const v = (location.hash || '').replace(/^#\//, '');
    return VIEWS[v] ? v : 'panel';
  }
  function route() {
    if ($('#app').hidden) return;
    const view = currentView();
    $$('dialog[open]').forEach((d) => d.close());
    $$('.nav a').forEach((a) => a.classList.toggle('active', a.dataset.view === view));
    $('#view-title').textContent = VIEWS[view][0];
    document.title = VIEWS[view][0] + ' · RM ↔ Shopify';
    $('#app').classList.remove('menu-open');
    VIEWS[view][1]().catch((err) => {
      $('#content').innerHTML = `<div class="card empty"><strong>No se pudo cargar</strong>${esc(err.message)}</div>`;
    });
  }
  window.addEventListener('hashchange', route);
  $('#menu-btn').addEventListener('click', () => $('#app').classList.add('menu-open'));
  $('#scrim').addEventListener('click', () => $('#app').classList.remove('menu-open'));
  $$('.theme-toggle').forEach((b) => b.addEventListener('click', toggleTheme));

  function paintMasterChip(settings, counts) {
    if (!settings) return;
    state.settings = settings;
    const m = MASTER[settings.master] || MASTER.RM;
    $('#master-chip').innerHTML =
      `<a class="badge badge-accent" href="#/configuracion" title="Sistema que manda en precio, descripción y altas">` +
      `<span class="dot"></span><span class="badge-label">Manda:</span> ${esc(m.short)}</a>` +
      (settings.createEnabled
        ? `<span class="badge badge-ok" title="El sync puede crear productos">${ICONS.unlock}<span class="badge-label">Altas activas</span></span>`
        : `<span class="badge badge-warn" title="El sync no crea productos hasta cerrar la conciliación">${ICONS.lock}<span class="badge-label">Altas bloqueadas</span></span>`);
    if (counts) {
      const pending = PENDING_KEYS.reduce((s, k) => s + (counts[k] || 0), 0);
      $('#nav-pending').textContent = pending ? pending.toLocaleString('es') : '';
    }
  }

  // ───────── Panel ─────────
  async function renderPanel() {
    const c = $('#content');
    c.innerHTML = '<div class="kpis">' + '<div class="card kpi"><div class="skeleton"></div></div>'.repeat(4) + '</div>';
    const d = await api('/dashboard');
    paintMasterChip(d.settings, d.counts);
    const counts = d.counts || {};
    const pending = PENDING_KEYS.reduce((s, k) => s + (counts[k] || 0), 0);
    const m = MASTER[d.settings.master] || MASTER.RM;
    const neverAnalyzed = !d.lastAnalysis && Object.values(counts).every((v) => !v);

    c.innerHTML = `
      ${neverAnalyzed ? `
      <div class="banner banner-warn">${ICONS.warn}
        <div><strong>Todavía no se han comparado los catálogos.</strong>
        <p class="muted">Ejecute el análisis para ver qué productos de Shopify ya existen en Retail Manager.</p></div>
        <div class="actions"><button class="btn btn-primary" data-act="analyze">${ICONS.refresh}Analizar catálogos</button></div>
      </div>` : ''}
      ${!d.settings.createEnabled && !neverAnalyzed ? `
      <div class="banner banner-warn">${ICONS.lock}
        <div><strong>Las altas automáticas están bloqueadas.</strong>
        <p class="muted">El sync solo actualiza productos ya vinculados. Revise la conciliación y ciérrela para permitir altas.</p></div>
        <div class="actions"><a class="btn" href="#/conciliacion">Ir a conciliación</a></div>
      </div>` : ''}
      <div class="kpis">
        <a class="card kpi" href="#/vinculados"><span class="kpi-label">Productos vinculados</span><span class="kpi-value">${num(d.linkedProducts)}</span><span class="kpi-sub">Relacionados en ProductSync</span></a>
        <a class="card kpi" href="#/conciliacion"><span class="kpi-label">Por revisar</span><span class="kpi-value">${num(pending)}</span><span class="kpi-sub">${num(counts.AUTO)} por código · ${num(counts.SUGERIDO)} sugeridos</span></a>
        <a class="card kpi" href="#/conciliacion" data-tab="SIN_MATCH"><span class="kpi-label">Solo en Shopify</span><span class="kpi-value">${num(counts.SIN_MATCH)}</span><span class="kpi-sub">${num(counts.AMBIGUO)} ambiguos</span></a>
        <a class="card kpi" href="#/conciliacion" data-tab="SOLO_RM"><span class="kpi-label">Solo en RM</span><span class="kpi-value">${num(counts.SOLO_RM)}</span><span class="kpi-sub">Con Web=True</span></a>
        <a class="card kpi" href="#/pedidos"><span class="kpi-label">Pedidos procesados</span><span class="kpi-value">${num(d.processedOrders)}</span><span class="kpi-sub">Último sync: ${date(d.lastOrderSync)}</span></a>
      </div>
      <div class="grid-2">
        <section class="card">
          <div class="card-head"><h2>Actividad reciente</h2><a href="#/auditoria" class="btn btn-ghost btn-sm">Ver todo</a></div>
          ${d.recentAudit.length ? `<ul class="activity">${d.recentAudit.map((a) => `
            <li><time>${date(a.at)}</time><span><strong>${esc(a.action)}</strong> · ${esc(a.username)}</span><span class="detail">${esc(a.detail)}</span></li>`).join('')}</ul>`
            : '<div class="empty">Sin actividad todavía</div>'}
        </section>
        <section class="card">
          <div class="card-head"><h2>Estado</h2><a href="#/configuracion" class="btn btn-ghost btn-sm">Configurar</a></div>
          <dl class="kv card-pad">
            <dt>Manda</dt><dd>${esc(m.label)}</dd>
            <dt>Stock</dt><dd>Siempre Retail Manager → Shopify</dd>
            <dt>Altas automáticas</dt><dd>${d.settings.createEnabled ? '<span class="badge badge-ok">Activas</span>' : '<span class="badge badge-warn">Bloqueadas</span>'}</dd>
            <dt>Borradores</dt><dd>${d.settings.importDrafts ? 'Se importan a RM' : 'No se importan a RM'}</dd>
            <dt>Último análisis</dt><dd>${date(d.lastAnalysis)}</dd>
          </dl>
        </section>
      </div>`;
    c.querySelector('[data-act=analyze]')?.addEventListener('click', analyze);
    $$('[data-tab]', c).forEach((a) => a.addEventListener('click', () => { state.recon.status = a.dataset.tab; state.recon.page = 0; }));
  }

  // ───────── Conciliación ─────────
  async function renderRecon() {
    const c = $('#content');
    const r = state.recon;
    if (!$('#recon-root', c)) {
      c.innerHTML = `
        <div id="recon-root" class="stack">
          <div class="section-head">
            <div><p class="muted" id="recon-sub">Relacione cada variante de Shopify con su producto de Retail Manager.</p></div>
            <div class="actions">
              <button class="btn" data-act="analyze">${ICONS.refresh}Analizar catálogos</button>
              <button class="btn" data-act="apply">${ICONS.wand}Aplicar recomendadas</button>
              <button class="btn btn-primary" data-act="close"></button>
            </div>
          </div>
          <div id="recon-banner"></div>
          <div class="tabs" role="tablist" id="recon-tabs"></div>
          <section class="card">
            <div class="toolbar">
              <label class="search">${ICONS.search}<input type="search" id="recon-q" placeholder="Buscar por nombre, SKU, código de barras o referencia"></label>
              <label class="switch"><input type="checkbox" id="recon-drafts"><span>Ocultar borradores</span></label>
              <span class="spacer"></span>
              <span class="muted" id="recon-total"></span>
            </div>
            <div class="bulkbar" id="recon-bulk" hidden></div>
            <div class="table-wrap" id="recon-table"></div>
            <div class="pager" id="recon-pager"></div>
          </section>
        </div>`;
      $('#recon-q').value = r.q;
      $('#recon-drafts').checked = r.hideDrafts;
      $('#recon-q').addEventListener('input', debounce((e) => { r.q = e.target.value; r.page = 0; loadRecon(); }));
      $('#recon-drafts').addEventListener('change', (e) => { r.hideDrafts = e.target.checked; r.page = 0; loadRecon(); });
      $('[data-act=analyze]', c).addEventListener('click', analyze);
      $('[data-act=apply]', c).addEventListener('click', applyRecommended);
      $('[data-act=close]', c).addEventListener('click', toggleClose);
    }
    await loadRecon();
  }

  async function loadRecon() {
    const r = state.recon;
    const params = new URLSearchParams({ status: r.status, page: r.page, size: 50, hideDrafts: r.hideDrafts });
    if (r.q.trim()) params.set('q', r.q.trim());
    $('#recon-table').innerHTML = '<div class="card-pad stack">' + '<div class="skeleton"></div>'.repeat(6) + '</div>';
    const [data, settings] = await Promise.all([api('/reconciliation?' + params), api('/settings')]);
    r.rows = data.rows;
    r.selected.clear();
    paintMasterChip(settings, data.counts);
    paintReconHeader(data, settings);
    paintTabs(data.counts);
    paintReconTable();
    paintBulk();
    paintPager($('#recon-pager'), data, (p) => { r.page = p; loadRecon(); });
    $('#recon-total').textContent = `${num(data.total)} filas`;
  }

  function paintReconHeader(data, settings) {
    const m = MASTER[settings.master] || MASTER.RM;
    $('#recon-sub').innerHTML = `Manda <strong>${esc(m.label)}</strong> · Último análisis: ${date(data.lastAnalysis)}`;
    const btn = $('[data-act=close]');
    btn.innerHTML = settings.createEnabled ? `${ICONS.lock}Reabrir conciliación` : `${ICONS.unlock}Cerrar conciliación`;
    btn.className = settings.createEnabled ? 'btn' : 'btn btn-primary';
    const counts = data.counts || {};
    const pending = PENDING_KEYS.reduce((s, k) => s + (counts[k] || 0), 0);
    $('#recon-banner').innerHTML = !data.lastAnalysis && !Object.values(counts).some(Boolean)
      ? `<div class="banner banner-warn">${ICONS.warn}<div><strong>Sin datos.</strong><p class="muted">Pulse “Analizar catálogos” para descargar Shopify y Retail Manager y compararlos.</p></div></div>`
      : settings.createEnabled
        ? `<div class="banner banner-ok">${ICONS.ok}<div><strong>Conciliación cerrada.</strong><p class="muted">El sync ya puede crear productos nuevos según quién manda.</p></div></div>`
        : pending
          ? `<div class="banner banner-warn">${ICONS.lock}<div><strong>Altas automáticas bloqueadas mientras concilia.</strong><p class="muted">Quedan ${num(pending)} filas por revisar. El sync solo actualiza productos ya vinculados.</p></div></div>`
          : '';
  }

  function paintTabs(counts) {
    const r = state.recon;
    const pending = PENDING_KEYS.reduce((s, k) => s + (counts[k] || 0), 0);
    const total = Object.values(counts).reduce((s, v) => s + v, 0);
    $('#recon-tabs').innerHTML = TABS.map(([key, label]) => {
      const n = key === 'PENDIENTES' ? pending : key === 'TODOS' ? total : (counts[key] || 0);
      return `<button class="tab ${r.status === key ? 'active' : ''}" role="tab" aria-selected="${r.status === key}" data-tab="${key}">${esc(label)}<span class="count">${n.toLocaleString('es')}</span></button>`;
    }).join('');
    $$('#recon-tabs .tab').forEach((b) => b.addEventListener('click', () => { r.status = b.dataset.tab; r.page = 0; loadRecon(); }));
  }

  function shopifyCell(row) {
    if (!row.shopifyVariantId) return '<span class="prod-none">No existe en Shopify</span>';
    const draft = row.shopifyStatus === 'DRAFT' ? '<span class="badge">Borrador</span>' : row.shopifyStatus === 'ARCHIVED' ? '<span class="badge">Archivado</span>' : '';
    return `<div class="prod">
      <span class="prod-title">${esc(row.shopifyTitle)}</span>
      ${row.shopifyVariantTitle ? `<span class="prod-meta">Variante: ${esc(row.shopifyVariantTitle)}${row.shopifyVariantCount > 1 ? ` · ${row.shopifyVariantCount} tamaños` : ''}</span>` : ''}
      <span class="prod-meta">${row.shopifySku ? `<span class="mono">SKU ${esc(cleanCode(row.shopifySku))}</span>` : '<span class="badge badge-warn">Sin SKU</span>'}
        ${row.shopifyBarcode && cleanCode(row.shopifyBarcode) !== cleanCode(row.shopifySku) ? `<span class="mono">CB ${esc(cleanCode(row.shopifyBarcode))}</span>` : ''}
        ${draft}</span>
      <span class="prod-meta"><span class="${row.priceDiff ? 'diff' : ''}">${money(row.shopifyPrice)}</span> · Stock ${num(row.shopifyQty)}</span>
    </div>`;
  }
  function rmCell(row) {
    if (!row.rmProductCode) {
      return row.status === 'AMBIGUO' || row.status === 'SIN_MATCH' ? '<span class="prod-none">Sin producto asignado</span>' : '<span class="prod-none">—</span>';
    }
    return `<div class="prod">
      <span class="prod-title">${esc(row.rmName || row.rmProductCode)}</span>
      <span class="prod-meta"><span class="mono">Ref ${esc(row.rmProductCode)}</span>${row.rmBarcode ? `<span class="mono">CB ${esc(row.rmBarcode)}</span>` : ''}</span>
      <span class="prod-meta"><span class="${row.priceDiff ? 'diff' : ''}">${money(row.rmPrice)}</span> · Stock ${num(row.rmStock)}</span>
    </div>`;
  }
  function rowActions(row) {
    const rec = row.recommendation;
    const b = (act, label, primary) => `<button class="btn btn-sm ${primary ? 'btn-primary' : ''}" data-row-act="${act}" data-id="${row.id}">${label}</button>`;
    switch (row.status) {
      case 'AUTO':
      case 'SUGERIDO':
        return b('link', 'Vincular', true) + b('assign', 'Cambiar…') + b('ignore', 'Ignorar');
      case 'AMBIGUO':
      case 'SIN_MATCH':
        return b('assign', 'Asignar…', rec !== 'CREATE_RM') + b('create-rm', 'Crear en RM', rec === 'CREATE_RM') + b('ignore', 'Ignorar', rec === 'IGNORE');
      case 'SOLO_RM':
        return b('create-shopify', 'Crear en Shopify', rec === 'CREATE_SHOPIFY') + b('ignore', 'Ignorar');
      case 'IGNORADO':
      case 'CREAR_EN_RM':
      case 'ERROR':
        return b('restore', 'Restaurar');
      default:
        return '';
    }
  }
  const REC_TEXT = {
    LINK: 'Vincular', REVIEW: 'Revisar y vincular', CHOOSE: 'Elegir el producto correcto', ASSIGN: 'Asignar o ignorar',
    CREATE_RM: 'Crear en RM', CREATE_SHOPIFY: 'Crear en Shopify', IGNORE: 'Ignorar (borrador)', REVIEW_RM: 'Revisar',
  };

  function paintReconTable() {
    const r = state.recon;
    if (!r.rows.length) {
      $('#recon-table').innerHTML = `<div class="empty"><strong>No hay filas en esta vista</strong>${r.q ? 'Pruebe con otra búsqueda.' : 'Cambie de pestaña o ejecute el análisis.'}</div>`;
      return;
    }
    $('#recon-table').innerHTML = `<table>
      <thead><tr>
        <th class="col-check"><input type="checkbox" id="recon-all" aria-label="Seleccionar todas"></th>
        <th>Estado</th><th>Shopify</th><th aria-hidden="true"></th><th>Retail Manager</th><th>Coincidencia</th><th class="num">Acciones</th>
      </tr></thead>
      <tbody>${r.rows.map((row) => `
        <tr data-id="${row.id}">
          <td class="col-check"><input type="checkbox" data-check="${row.id}" aria-label="Seleccionar fila"></td>
          <td>${badge(row.status)}</td>
          <td>${shopifyCell(row)}</td>
          <td class="arrow">${ICONS.arrow}</td>
          <td>${rmCell(row)}${row.note ? `<p class="note">${esc(row.note)}</p>` : ''}</td>
          <td class="nowrap">
            ${row.method ? `<div>${esc(METHOD[row.method] || row.method)}</div>` : ''}
            ${row.score != null && row.method === 'NOMBRE' ? `<div class="muted score">${Math.round(row.score * 100)}% parecido</div>` : ''}
            ${row.priceDiff ? '<div class="diff">Precio distinto</div>' : ''}
            ${REC_TEXT[row.recommendation] ? `<div class="muted">Sugerido: ${esc(REC_TEXT[row.recommendation])}</div>` : ''}
          </td>
          <td><div class="row-actions">${rowActions(row)}</div></td>
        </tr>`).join('')}
      </tbody></table>`;
    $('#recon-all').addEventListener('change', (e) => {
      $$('[data-check]').forEach((cb) => { cb.checked = e.target.checked; toggleSelect(Number(cb.dataset.check), cb.checked); });
    });
    $$('[data-check]').forEach((cb) => cb.addEventListener('change', () => toggleSelect(Number(cb.dataset.check), cb.checked)));
    $$('[data-row-act]').forEach((btn) => btn.addEventListener('click', () => rowAction(btn.dataset.rowAct, [Number(btn.dataset.id)])));
  }

  function toggleSelect(id, on) {
    const r = state.recon;
    on ? r.selected.add(id) : r.selected.delete(id);
    $(`tr[data-id="${id}"]`)?.classList.toggle('selected', on);
    paintBulk();
  }
  function paintBulk() {
    const r = state.recon;
    const bar = $('#recon-bulk');
    if (!r.selected.size) { bar.hidden = true; return; }
    const rows = r.rows.filter((x) => r.selected.has(x.id));
    const has = (...st) => rows.some((x) => st.includes(x.status));
    bar.hidden = false;
    bar.innerHTML = `<strong>${r.selected.size} seleccionadas</strong>
      ${has('AUTO', 'SUGERIDO') ? '<button class="btn btn-sm btn-primary" data-bulk="link">Vincular</button>' : ''}
      ${has('SIN_MATCH', 'AMBIGUO') ? '<button class="btn btn-sm" data-bulk="create-rm">Crear en RM</button>' : ''}
      ${has('SOLO_RM') ? '<button class="btn btn-sm" data-bulk="create-shopify">Crear en Shopify</button>' : ''}
      ${has('AUTO', 'SUGERIDO', 'AMBIGUO', 'SIN_MATCH', 'SOLO_RM', 'ERROR') ? '<button class="btn btn-sm" data-bulk="ignore">Ignorar</button>' : ''}
      ${has('IGNORADO', 'CREAR_EN_RM', 'ERROR') ? '<button class="btn btn-sm" data-bulk="restore">Restaurar</button>' : ''}
      <button class="btn btn-sm btn-ghost" data-bulk="clear">Quitar selección</button>`;
    $$('[data-bulk]', bar).forEach((b) => b.addEventListener('click', () => {
      if (b.dataset.bulk === 'clear') { [...r.selected].forEach((id) => { const cb = $(`[data-check="${id}"]`); if (cb) cb.checked = false; toggleSelect(id, false); }); return; }
      rowAction(b.dataset.bulk, [...r.selected]);
    }));
  }

  async function rowAction(act, ids) {
    try {
      if (act === 'assign') { openAssign(state.recon.rows.find((x) => x.id === ids[0])); return; }
      if (act === 'ignore' || act === 'restore') {
        await api('/reconciliation/' + act, { method: 'POST', body: { ids } });
        toast(act === 'ignore' ? `${ids.length} fila(s) ignorada(s)` : `${ids.length} fila(s) restaurada(s)`);
        loadRecon();
        return;
      }
      if (act === 'create-shopify' && ids.length > 1) {
        const ok = await confirmDialog('Crear en Shopify', `Se crearán ${ids.length} productos nuevos en Shopify y se publicarán en todos los canales.`, 'Crear');
        if (!ok) return;
      }
      if (act === 'create-rm' && ids.length > 1) {
        const ok = await confirmDialog('Crear en Retail Manager', `Se crearán ${ids.length} productos en Retail Manager con el precio de Shopify y se marcarán como productos web.`, 'Crear');
        if (!ok) return;
      }
      const job = await api('/reconciliation/' + act, { method: 'POST', body: { ids } });
      startJobPolling(job);
    } catch (err) {
      toast(err.message, 'error');
    }
  }

  async function analyze() {
    try { startJobPolling(await api('/reconciliation/analyze', { method: 'POST' })); } catch (err) { toast(err.message, 'error'); }
  }

  async function applyRecommended() {
    const s = state.settings || (await api('/settings'));
    const m = MASTER[s.master] || MASTER.RM;
    const extra = s.master === 'SHOPIFY' ? ' Las variantes de Shopify sin coincidencia se crearán en RM.'
      : s.master === 'RM' ? ' Los productos que solo están en RM se crearán en Shopify.'
      : ' Como no manda ningún sistema, no se creará nada: lo demás queda para revisión manual.';
    const ok = await confirmDialog('Aplicar acciones recomendadas',
      `Se vincularán todas las coincidencias por código.${extra} Los sugeridos por nombre y los ambiguos NO se tocan. Manda: ${m.label}.`, 'Aplicar');
    if (!ok) return;
    try { startJobPolling(await api('/reconciliation/apply-recommended', { method: 'POST' })); } catch (err) { toast(err.message, 'error'); }
  }

  async function toggleClose() {
    const s = await api('/settings');
    const reopen = s.createEnabled;
    const ok = await confirmDialog(reopen ? 'Reabrir conciliación' : 'Cerrar conciliación',
      reopen ? 'El sync dejará de crear productos nuevos hasta que vuelva a cerrar la conciliación.'
        : 'El sync empezará a crear productos nuevos según quién manda. Hágalo cuando ya no queden productos por relacionar.',
      reopen ? 'Reabrir' : 'Cerrar conciliación');
    if (!ok) return;
    try {
      await api('/reconciliation/close', { method: 'POST', body: { reopen } });
      toast(reopen ? 'Conciliación reabierta: altas bloqueadas' : 'Conciliación cerrada: altas activas');
      loadRecon();
    } catch (err) { toast(err.message, 'error'); }
  }

  // Diálogo de asignación
  let assignRow = null;
  function openAssign(row) {
    if (!row) return;
    assignRow = row;
    $('#assign-subtitle').textContent = `Shopify: ${row.shopifyTitle}${row.shopifyVariantTitle ? ' · ' + row.shopifyVariantTitle : ''}`;
    const input = $('#assign-search');
    input.value = row.rmName || (row.shopifyTitle || '').replace(/\s+BY\s+.*$/i, '');
    $('#assign-dialog').showModal();
    input.focus();
    input.select();
    searchAssign();
  }
  const searchAssign = debounce(async () => {
    const list = $('#assign-results');
    list.innerHTML = '<li class="empty">Buscando…</li>';
    try {
      const results = await api('/rm-products?q=' + encodeURIComponent($('#assign-search').value));
      list.innerHTML = results.length ? results.map((p) => `
        <li><button type="button" data-code="${esc(p.productCode)}">
          <span class="prod"><span class="prod-title">${esc(p.productName)}</span>
          <span class="prod-meta"><span class="mono">Ref ${esc(p.productCode)}</span>${p.barCode ? `<span class="mono">CB ${esc(p.barCode)}</span>` : ''}</span></span>
          <span class="prod-meta nowrap">${money(p.price)} · Stock ${num(p.stock)}</span>
        </button></li>`).join('') : '<li class="empty">Sin resultados</li>';
      $$('button[data-code]', list).forEach((b) => b.addEventListener('click', () => doAssign(b.dataset.code)));
    } catch (err) {
      list.innerHTML = `<li class="empty">${esc(err.message)}</li>`;
    }
  }, 250);
  $('#assign-search').addEventListener('input', searchAssign);
  async function doAssign(code) {
    try {
      await api('/reconciliation/assign', { method: 'POST', body: { id: assignRow.id, rmProductCode: code } });
      $('#assign-dialog').close();
      toast(`Vinculado con ${code}`);
      loadRecon();
    } catch (err) { toast(err.message, 'error'); }
  }

  // ───────── Configuración ─────────
  async function renderSettings() {
    const s = await api('/settings');
    paintMasterChip(s);
    const choice = (value, title, items) => `
      <label class="choice"><input type="radio" name="master" value="${value}" ${s.master === value ? 'checked' : ''}>
        <span class="choice-title">${esc(title)}</span><ul>${items.map((i) => `<li>${esc(i)}</li>`).join('')}</ul></label>`;
    $('#content').innerHTML = `
      <form id="settings-form" class="card">
        <div class="card-head"><div><h2>¿Qué sistema manda?</h2><p class="muted">Decide el precio, la descripción y la creación de productos. El stock siempre va de Retail Manager a Shopify.</p></div></div>
        <div class="card-pad">
          <div class="choice-grid">
            ${choice('RM', 'Retail Manager', ['Precio y descripción de RM pasan a Shopify', 'Productos nuevos en RM (Web=True) se crean en Shopify', 'Si se borra en RM, se borra en Shopify'])}
            ${choice('SHOPIFY', 'Shopify', ['Precio y descripción no se tocan en Shopify', 'Productos solo en Shopify se crean en RM', 'Nunca se borra nada en Shopify'])}
            ${choice('NINGUNO', 'Ninguno', ['Cada sistema conserva su precio', 'La factura usa el precio de venta de Shopify', 'Los productos nuevos se resuelven en conciliación'])}
          </div>
        </div>
        <div class="settings-row">
          <div><h3>Importar borradores a RM</h3><p>Los borradores existen en Shopify pero no están a la venta en ningún canal. Si está apagado, se concilian pero no se crean en RM.</p></div>
          <label class="switch"><input type="checkbox" name="importDrafts" ${s.importDrafts ? 'checked' : ''}><span>${s.importDrafts ? 'Sí' : 'No'}</span></label>
        </div>
        <div class="settings-row">
          <div><h3>Límite de borrado por ciclo</h3><p>Si en un ciclo desaparece de RM más de este porcentaje de productos vinculados, el borrado en Shopify se bloquea y queda en auditoría.</p></div>
          <label class="field"><input type="number" name="maxDeletePercent" min="0" max="100" value="${esc(s.maxDeletePercent)}"><small>Porcentaje (0–100)</small></label>
        </div>
        <div class="settings-row">
          <div><h3>Altas automáticas</h3><p>Se activan al cerrar la conciliación. Mientras estén bloqueadas, el sync solo actualiza productos ya vinculados.</p></div>
          <div>${s.createEnabled ? '<span class="badge badge-ok">Activas</span>' : '<span class="badge badge-warn">Bloqueadas</span>'} <a href="#/conciliacion" class="btn btn-ghost btn-sm">Ir a conciliación</a></div>
        </div>
        <div class="save-bar"><button type="reset" class="btn">Descartar</button><button type="submit" class="btn btn-primary">Guardar cambios</button></div>
      </form>

      <form id="password-form" class="card">
        <div class="card-head"><div><h2>Cuenta</h2><p class="muted">Usuario: ${esc(state.user)}</p></div></div>
        <div class="settings-row"><div><h3>Contraseña actual</h3></div><input type="password" name="current" autocomplete="current-password" required></div>
        <div class="settings-row"><div><h3>Nueva contraseña</h3><p>Mínimo 10 caracteres.</p></div><input type="password" name="next" autocomplete="new-password" minlength="10" required></div>
        <div class="settings-row"><div><h3>Repita la nueva contraseña</h3></div><input type="password" name="confirm" autocomplete="new-password" minlength="10" required></div>
        <div class="save-bar"><span class="form-error" style="margin-right:auto"></span><button type="submit" class="btn btn-primary">Cambiar contraseña</button></div>
      </form>`;

    const form = $('#settings-form');
    const sw = $('[name=importDrafts]', form);
    sw.addEventListener('change', () => { sw.nextElementSibling.textContent = sw.checked ? 'Sí' : 'No'; });
    form.addEventListener('reset', () => setTimeout(renderSettings, 0));
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const master = $('[name=master]:checked', form).value;
      if (master !== s.master) {
        const ok = await confirmDialog('Cambiar quién manda', `Pasará a mandar ${MASTER[master].label}. El próximo ciclo del sync aplicará las nuevas reglas.`, 'Cambiar');
        if (!ok) return;
      }
      try {
        const saved = await api('/settings', { method: 'PUT', body: {
          master,
          importDrafts: sw.checked,
          maxDeletePercent: Number($('[name=maxDeletePercent]', form).value),
        } });
        paintMasterChip(saved);
        toast('Configuración guardada');
        renderSettings();
      } catch (err) { toast(err.message, 'error'); }
    });

    const pf = $('#password-form');
    pf.addEventListener('submit', async (e) => {
      e.preventDefault();
      const errEl = $('.form-error', pf);
      errEl.textContent = '';
      if (pf.next.value !== pf.confirm.value) { errEl.textContent = 'Las contraseñas nuevas no coinciden'; return; }
      try {
        await api('/auth/change-password', { method: 'POST', body: { current: pf.current.value, next: pf.next.value } });
        pf.reset();
        toast('Contraseña actualizada');
      } catch (err) { errEl.textContent = err.message; }
    });
  }

  // ───────── Tablas simples ─────────
  function paintPager(el, data, go) {
    if (!data.pages || data.pages <= 1) { el.innerHTML = data.total ? `<span>${num(data.total)} en total</span>` : ''; return; }
    el.innerHTML = `<span>Página ${data.page + 1} de ${data.pages} · ${num(data.total)} en total</span>
      <span class="actions"><button class="btn btn-sm" data-p="${data.page - 1}" ${data.page === 0 ? 'disabled' : ''}>Anterior</button>
      <button class="btn btn-sm" data-p="${data.page + 1}" ${data.page + 1 >= data.pages ? 'disabled' : ''}>Siguiente</button></span>`;
    $$('[data-p]', el).forEach((b) => b.addEventListener('click', () => go(Number(b.dataset.p))));
  }

  async function simpleTable({ key, path, searchPlaceholder, extraToolbar = '', columns, emptyText, bind }) {
    const st = state[key];
    const c = $('#content');
    if (!$(`#${key}-root`, c)) {
      c.innerHTML = `<section class="card" id="${key}-root">
        <div class="toolbar">
          ${searchPlaceholder ? `<label class="search">${ICONS.search}<input type="search" id="${key}-q" placeholder="${esc(searchPlaceholder)}"></label>` : ''}
          ${extraToolbar}<span class="spacer"></span>
        </div>
        <div class="table-wrap" id="${key}-table"></div><div class="pager" id="${key}-pager"></div></section>`;
      const q = $(`#${key}-q`);
      if (q) { q.value = st.q || ''; q.addEventListener('input', debounce((e) => { st.q = e.target.value; st.page = 0; simpleTable(arguments[0]); })); }
      bind?.(c, () => simpleTable(arguments[0]));
    }
    const params = new URLSearchParams({ page: st.page });
    if (st.q && st.q.trim()) params.set('q', st.q.trim());
    if (st.includeDeleted) params.set('includeDeleted', 'true');
    const data = await api(path + '?' + params);
    $(`#${key}-table`).innerHTML = data.rows.length
      ? `<table><thead><tr>${columns.map((col) => `<th class="${col.cls || ''}">${esc(col.title)}</th>`).join('')}</tr></thead>
         <tbody>${data.rows.map((row) => `<tr>${columns.map((col) => `<td class="${col.cls || ''}">${col.render(row)}</td>`).join('')}</tr>`).join('')}</tbody></table>`
      : `<div class="empty"><strong>Sin resultados</strong>${esc(emptyText)}</div>`;
    paintPager($(`#${key}-pager`), data, (p) => { st.page = p; simpleTable(arguments[0]); });
  }

  function renderLinks() {
    return simpleTable({
      key: 'links', path: '/links', searchPlaceholder: 'Buscar por referencia de RM o ID de Shopify',
      extraToolbar: '<label class="switch"><input type="checkbox" id="links-deleted"><span>Incluir eliminados</span></label>',
      emptyText: 'Todavía no hay productos vinculados.',
      bind: (c, reload) => {
        const cb = $('#links-deleted', c);
        cb.checked = state.links.includeDeleted;
        cb.addEventListener('change', () => { state.links.includeDeleted = cb.checked; state.links.page = 0; reload(); });
      },
      columns: [
        { title: 'Referencia RM', render: (r) => `<span class="mono">${esc(r.productCode)}</span>` },
        { title: 'Producto Shopify', render: (r) => `<span class="mono">${esc((r.shopifyProductId || '').split('/').pop())}</span>` },
        { title: 'Variante', render: (r) => `<span class="mono">${esc((r.shopifyVariantId || '').split('/').pop())}</span>` },
        { title: 'Estado', render: (r) => r.deleted ? '<span class="badge badge-danger">Eliminado</span>' : r.pending ? '<span class="badge badge-info">Pendiente de enviar</span>' : '<span class="badge badge-ok">Sincronizado</span>' },
        { title: 'Último envío', cls: 'nowrap', render: (r) => date(r.lastSync) },
        { title: 'Visto en RM', cls: 'nowrap', render: (r) => date(r.lastSeen) },
      ],
    });
  }

  const EVENT = { PAID: ['Pagado', 'badge-ok'], CANCELLED: ['Cancelado', 'badge-danger'], REFUNDED: ['Reembolsado', 'badge-warn'], PARTIALLY_REFUNDED: ['Reembolso parcial', 'badge-warn'], PARTIALLY_PAID: ['Pago parcial', 'badge-warn'], VOIDED: ['Anulado', 'badge-danger'], PENDING: ['Pendiente', 'badge-info'] };
  function renderOrders() {
    return simpleTable({
      key: 'orders', path: '/orders', searchPlaceholder: 'Buscar por ID de pedido, factura de RM o estado',
      emptyText: 'Todavía no se ha procesado ningún pedido.',
      columns: [
        { title: 'Pedido Shopify', render: (r) => `<strong>${esc(r.orderName || '—')}</strong><div class="muted mono">${esc((r.id || '').split('/').pop())}</div>` },
        // K… = lo envió el ciclo de sincronización; KW… = lo envió el webhook de pago
        { title: 'Factura en RM', render: (r) => r.rmInvoice
          ? `<span class="mono">${esc(r.rmInvoice)}</span><div class="muted">${String(r.rmInvoice).startsWith('KW') ? 'Webhook' : 'Ciclo'}</div>`
          : '<span class="muted">—</span>' },
        { title: 'Estado', render: (r) => { const [l, c] = EVENT[r.eventType] || [r.eventType || '—', '']; return `<span class="badge ${c}">${esc(l)}</span>`; } },
        { title: 'Total', cls: 'num', render: (r) => money(r.totalAmount) },
        { title: 'Procesado', cls: 'nowrap', render: (r) => date(r.processedAt) },
      ],
    });
  }

  // Facturas que RM rechazó o no recibió (FailedOrder) + vista previa por ID de pedido
  function renderFailedOrders() {
    return simpleTable({
      key: 'failed', path: '/failed-orders', searchPlaceholder: 'Buscar por pedido o error',
      extraToolbar: `<label class="search"><input type="search" id="preview-id" placeholder="ID de pedido de Shopify"></label>
        <button class="btn btn-sm" id="preview-btn" type="button">Vista previa</button>`,
      emptyText: 'Todas las facturas llegaron a RM.',
      bind: (c, reload) => {
        const root = $('#failed-root', c);
        const preview = () => { const id = $('#preview-id', root).value.trim(); if (id) showInvoicePreview(id); };
        $('#preview-btn', root).addEventListener('click', preview);
        $('#preview-id', root).addEventListener('keydown', (e) => { if (e.key === 'Enter') preview(); });
        root.addEventListener('click', async (e) => {
          const b = e.target.closest('[data-preview]');
          if (b) { showInvoicePreview(b.dataset.preview); return; }
          const r = e.target.closest('[data-retry]');
          if (!r) return;
          r.disabled = true;
          try {
            const res = await api('/failed-orders/retry', { method: 'POST', body: { id: r.dataset.retry } });
            res.sent ? toast('Factura enviada a RM') : toast('Falló de nuevo: ' + (res.lastError || ''), 'error');
          } catch (err) { toast(err.message, 'error'); }
          reload();
        });
      },
      columns: [
        { title: 'Pedido', render: (r) => `<span class="mono">${esc(r.orderName || (r.id || '').split('/').pop())}</span>` },
        { title: 'Intentos', cls: 'num', render: (r) => r.attempts >= r.maxAttempts
          ? `<span class="badge badge-danger">${num(r.attempts)} · revisar</span>` : `<span class="badge badge-warn">${num(r.attempts)} de ${num(r.maxAttempts)}</span>` },
        { title: 'Último error', render: (r) => `<span class="muted">${esc(r.lastError)}</span>` },
        { title: 'Último intento', cls: 'nowrap', render: (r) => date(r.lastAttemptAt) },
        { title: '', cls: 'nowrap', render: (r) => `<button class="btn btn-sm" type="button" data-preview="${esc(r.id)}">Vista previa</button>
          <button class="btn btn-sm" type="button" data-retry="${esc(r.id)}">Reintentar</button>` },
      ],
    });
  }

  async function showInvoicePreview(id) {
    const dlg = $('#preview-dialog');
    $('#preview-subtitle').textContent = id;
    $('#preview-body').innerHTML = '<p class="muted">Consultando Shopify…</p>';
    if (!dlg.open) dlg.showModal();
    try {
      const p = await api('/orders/preview?' + new URLSearchParams({ id }));
      const inv = p.invoice;
      const status = [
        p.ruleOk ? '<span class="badge badge-ok">Montos OK</span>' : `<span class="badge badge-danger">${esc(p.ruleError)}</span>`,
        p.wouldSend ? '<span class="badge badge-info">Se enviaría (PAID)</span>' : `<span class="badge badge-warn">No se envía (${esc(p.financialStatus)})</span>`,
        p.alreadyProcessed ? '<span class="badge badge-ok">Ya procesado</span>' : '',
        p.failedAttempts ? `<span class="badge badge-warn">${num(p.failedAttempts)} intento(s) fallido(s)</span>` : '',
      ].join(' ');
      $('#preview-subtitle').textContent = `Pedido ${p.orderName} · Factura ${inv.Factura}`;
      $('#preview-body').innerHTML = `<p>${status}</p>
        <div class="table-wrap"><table><tbody>
          ${['Subtotal', 'Descuento', 'CityTax', 'StateTax', 'Total', 'PaidCash'].map((k) => `<tr><td>${k}</td><td class="num">${money(inv[k])}</td></tr>`).join('')}
        </tbody></table></div>
        <div class="table-wrap"><table><thead><tr><th>Referencia</th><th class="num">Cant.</th><th class="num">Precio</th><th class="num">Total</th></tr></thead><tbody>
          ${(inv.Items || []).map((i) => `<tr><td class="mono">${esc(i.Referencia)}</td><td class="num">${num(i.CantidadDespachada)}</td><td class="num">${money(i.Precio)}</td><td class="num">${money(i.TotalF)}</td></tr>`).join('')}
        </tbody></table></div>`;
    } catch (err) {
      $('#preview-body').innerHTML = `<p class="form-error">${esc(err.message)}</p>`;
    }
  }

  // Traza de los webhooks de Shopify (WebhookLog): qué llegó, qué se hizo y el error si lo hubo
  const WEBHOOK_RESULT = {
    PROCESADA: ['Procesada', 'badge-ok'], RECIBIDA: ['Recibida', 'badge-info'], OK: ['OK', 'badge-ok'],
    DUPLICADA: ['Duplicada', ''], IGNORADA: ['Ignorada', ''],
    FIRMA_INVALIDA: ['Firma inválida', 'badge-danger'], SIN_FIRMA: ['Sin firma', 'badge-danger'],
    PETICION_INVALIDA: ['Petición inválida', 'badge-warn'], ERROR: ['Error', 'badge-danger'],
  };
  const webhookBadge = (r) => { const [l, c] = WEBHOOK_RESULT[r] || [r || '—', '']; return `<span class="badge ${c}">${esc(l)}</span>`; };

  async function renderWebhooks() {
    await simpleTable({
      key: 'webhooks', path: '/webhooks', searchPlaceholder: 'Buscar por pedido, tema, resultado o error',
      emptyText: 'Todavía no ha llegado ningún webhook.',
      bind: (c) => {
        $('#webhooks-root', c).addEventListener('click', (e) => {
          const b = e.target.closest('[data-webhook]');
          if (b) showWebhookDetail(b.dataset.webhook);
        });
      },
      columns: [
        { title: 'Recibido', cls: 'nowrap', render: (r) => date(r.receivedAt) },
        { title: 'Endpoint', render: (r) => `<span class="mono">${esc((r.endpoint || '').replace('/webhook', ''))}</span>${r.topic ? `<div class="muted">${esc(r.topic)}</div>` : ''}` },
        { title: 'Pedido', render: (r) => `<span class="mono">${esc(r.orderName || (r.shopifyOrderId || '').split('/').pop() || '—')}</span>` },
        { title: 'Resultado', render: (r) => webhookBadge(r.result) + (r.error ? `<div class="muted">${esc(r.error.slice(0, 120))}</div>` : '') },
        { title: 'HTTP', cls: 'num', render: (r) => esc(r.httpStatus) },
        { title: 'ms', cls: 'num', render: (r) => num(r.durationMs) },
        { title: '', cls: 'nowrap', render: (r) => `<button class="btn btn-sm" type="button" data-webhook="${esc(r.id)}">Ver</button>` },
      ],
    });
    const root = $('#webhooks-root');
    if (root && !$('#webhook-config')) {
      root.insertAdjacentHTML('beforebegin', '<section class="card" id="webhook-config"><div class="card-pad muted">Cargando configuración…</div></section>');
      try { paintWebhookConfig(await api('/webhooks/config')); } catch (err) { toast(err.message, 'error'); }
    }
  }

  // Configuración de los webhooks en Shopify: URL pública, llave de firma, modo de cada uno e instrucciones
  const WEBHOOK_MODES = { ACTIVO: 'Activo', SOLO_REGISTRO: 'Solo registrar' };
  const SECRET_SOURCE = {
    PANEL: ['Configurada en el panel', 'badge-ok'],
    INSTALADOR: ['Usando la del instalador', 'badge-info'],
    NINGUNA: ['Sin llave: la firma de todos los webhooks fallará', 'badge-danger'],
  };

  function paintWebhookConfig(cfg) {
    const card = $('#webhook-config');
    if (!card) return;
    const [srcLabel, srcCls] = SECRET_SOURCE[cfg.secretSource] || [cfg.secretSource, ''];
    card.innerHTML = `
      <div class="card-head"><div><h2>Configuración en Shopify</h2>
        <p class="muted">Direcciones para dar de alta cada webhook en Shopify, llave con que se comprueba su firma y modo de cada uno.</p></div></div>
      <form id="webhook-config-form">
        <div class="settings-row">
          <div><h3>URL pública del servicio</h3><p>Dirección HTTPS con la que Shopify llega a este servicio desde internet. Con ella se arman las URL de la tabla.</p></div>
          <label class="field"><input name="publicUrl" placeholder="https://rmpay.mitienda.com" value="${esc(cfg.publicUrl)}"><small>Debe empezar con https://</small></label>
        </div>
        <div class="settings-row">
          <div><h3>Llave de firma</h3><p>La que muestra Shopify debajo de la lista de webhooks: “Tus webhooks se firmarán con …”. Se aplica al instante, sin reiniciar.</p></div>
          <div class="field">
            <span><span class="badge ${srcCls}">${esc(srcLabel)}</span> <span class="mono">${esc(cfg.secretHint)}</span></span>
            <input type="password" name="secret" autocomplete="new-password" placeholder="Pegue aquí la llave nueva">
            ${cfg.secretSource === 'PANEL' ? '<label class="switch"><input type="checkbox" name="clearSecret"><span>Volver a la llave del instalador</span></label>' : ''}
          </div>
        </div>
        <div class="table-wrap"><table>
          <thead><tr><th>Evento en Shopify</th><th>URL para Shopify</th><th>Modo</th><th>Último recibido</th></tr></thead>
          <tbody>${cfg.webhooks.map((w) => `<tr>
            <td><strong>${esc(w.label)}</strong><div class="muted mono">${esc(w.topic)}</div></td>
            <td>${w.url
              ? `<span class="mono">${esc(w.url)}</span> <button class="btn btn-sm" type="button" data-copy="${esc(w.url)}">Copiar</button>`
              : `<span class="muted">Configure la URL pública (ruta <span class="mono">${esc(w.path)}</span>)</span>`}</td>
            <td><select name="mode-${esc(w.key)}">${Object.entries(WEBHOOK_MODES).map(([v, l]) => `<option value="${v}" ${w.mode === v ? 'selected' : ''}>${l}</option>`).join('')}</select></td>
            <td class="nowrap">${w.lastReceivedAt
              ? `${date(w.lastReceivedAt)}<div>${webhookBadge(w.lastResult)}${w.lastHmacValid === false ? ' <span class="badge badge-danger">Firma inválida</span>' : ''}</div>`
              : '<span class="muted">Nunca</span>'}</td>
          </tr>`).join('')}</tbody>
        </table></div>
        <details class="card-pad steps">
          <summary><strong>Cómo configurarlos en Shopify</strong></summary>
          <ol>
            <li><strong>Requisito:</strong> Shopify solo envía webhooks a una dirección <strong>HTTPS pública con certificado válido</strong>. El certificado local del servicio no sirve: publique el puerto de la API del servicio (9192 por defecto) con un dominio y un proxy (IIS, nginx) o con un túnel (por ejemplo Cloudflare Tunnel).</li>
            <li>Escriba esa dirección en <em>URL pública del servicio</em> y guarde. La tabla mostrará las URL completas.</li>
            <li>En Shopify entre a <em>Configuración → Notificaciones → Webhooks</em> (al final de la página) y use <em>Crear webhook</em>.</li>
            <li>Cree uno por cada fila de la tabla: <em>Evento</em> = el de la columna “Evento en Shopify”, <em>Formato</em> = JSON, <em>URL</em> = la de la tabla (botón Copiar), <em>Versión de la API</em> = ${esc(cfg.apiVersion || 'la más reciente')}.</li>
            <li>Copie la llave que Shopify muestra debajo de la lista (“Tus webhooks se firmarán con …”), péguela en <em>Llave de firma</em> y guarde.</li>
            <li>Para probar sin crear facturas: ponga el webhook en <strong>Solo registrar</strong>, use <em>Enviar notificación de prueba</em> en Shopify y compruebe en la lista de abajo que llegó con la firma válida. Después vuelva a <strong>Activo</strong>. Ojo: con <em>Pago de pedido</em> en “Activo”, la notificación de prueba crea una factura de prueba en RM.</li>
            <li>Si cambia la llave o la dirección en Shopify, actualícelas aquí. “Solo registrar” no detiene los envíos de Shopify: para dejar de recibir un webhook, bórrelo en Shopify.</li>
          </ol>
        </details>
        <div class="save-bar"><button type="submit" class="btn btn-primary">Guardar configuración</button></div>
      </form>`;

    const form = $('#webhook-config-form', card);
    $$('[data-copy]', form).forEach((b) => b.addEventListener('click', async () => {
      try { await navigator.clipboard.writeText(b.dataset.copy); toast('URL copiada'); } catch (e) { toast('No se pudo copiar: selecciónela y cópiela a mano', 'error'); }
    }));
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const modes = {};
      cfg.webhooks.forEach((w) => { modes[w.key] = $(`[name="mode-${w.key}"]`, form).value; });
      const clear = $('[name=clearSecret]', form);
      try {
        const saved = await api('/webhooks/config', { method: 'PUT', body: {
          publicUrl: $('[name=publicUrl]', form).value.trim(),
          secret: $('[name=secret]', form).value,
          clearSecret: !!(clear && clear.checked),
          modes,
        } });
        paintWebhookConfig(saved);
        toast('Configuración de webhooks guardada');
      } catch (err) { toast(err.message, 'error'); }
    });
  }

  const prettyJson = (text) => { try { return JSON.stringify(JSON.parse(text), null, 2); } catch (e) { return text || ''; } };
  const codeSection = (title, text) => text ? `<h3 class="section-title">${esc(title)}</h3><pre class="code-block">${esc(text)}</pre>` : '';

  async function showWebhookDetail(id) {
    const dlg = $('#webhook-dialog');
    $('#webhook-subtitle').textContent = '';
    $('#webhook-body').innerHTML = '<p class="muted">Cargando…</p>';
    if (!dlg.open) dlg.showModal();
    try {
      const w = await api('/webhooks/' + encodeURIComponent(id));
      const yesNo = (v) => (v == null ? '—' : v ? 'Sí' : 'No');
      $('#webhook-subtitle').textContent = `${w.endpoint} · ${date(w.receivedAt)}`;
      $('#webhook-body').innerHTML = `<p>${webhookBadge(w.result)} <span class="muted">HTTP ${esc(w.httpStatus)} · ${num(w.durationMs)} ms</span></p>
        ${w.detail ? `<p>${esc(w.detail)}</p>` : ''}
        <div class="table-wrap"><table><tbody>
          <tr><td>Pedido</td><td class="mono">${esc(w.orderName || '—')} ${esc(w.shopifyOrderId || '')}</td></tr>
          <tr><td>Tema (X-Shopify-Topic)</td><td class="mono">${esc(w.topic || '—')}</td></tr>
          <tr><td>ID del webhook</td><td class="mono">${esc(w.webhookId || '—')}</td></tr>
          <tr><td>Tienda</td><td class="mono">${esc(w.shopDomain || '—')}</td></tr>
          <tr><td>Firma recibida / válida</td><td>${yesNo(w.hmacPresent)} / ${yesNo(w.hmacValid)}</td></tr>
        </tbody></table></div>
        ${codeSection('Error', w.error)}
        ${codeSection('Traza del error', w.stackTrace)}
        ${codeSection('Factura enviada a RM', w.invoiceJson)}
        ${codeSection('Respuesta enviada a Shopify', w.responseBody)}
        ${codeSection('Body recibido', prettyJson(w.payload))}`;
    } catch (err) {
      $('#webhook-body').innerHTML = `<p class="form-error">${esc(err.message)}</p>`;
    }
  }

  // ───────── Conciliación automática (Configuración) ─────────
  // Tarjeta agregada a la vista Configuración sin tocar renderSettings: cada vez que aparece el
  // formulario de ajustes (#settings-form), se inserta debajo. API: AdminAutoReconciliationController.
  async function paintAutoReconciliation() {
    const form = $('#settings-form');
    if (!form || $('#auto-recon')) return;
    form.insertAdjacentHTML('afterend', '<form id="auto-recon" class="card"><div class="card-pad muted">Cargando conciliación automática…</div></form>');
    let a;
    try { a = await api('/reconciliation/auto'); } catch (err) { $('#auto-recon').innerHTML = `<div class="card-pad form-error">${esc(err.message)}</div>`; return; }
    const card = $('#auto-recon');
    if (!card) return;
    card.innerHTML = `
      <div class="card-head"><div><h2>Conciliación automática</h2>
        <p class="muted">Cada cierto tiempo analiza los catálogos y aplica las acciones recomendadas (vincular por código y crear en RM o en Shopify según quién manda), igual que los botones de Conciliación. Los sugeridos y ambiguos siguen esperando revisión.</p></div></div>
      <div class="settings-row">
        <div><h3>Cada cuántos minutos</h3><p>0 la apaga. Cuenta desde el último análisis, automático o manual.</p></div>
        <label class="field"><input type="number" name="minutes" min="0" max="1440" value="${esc(a.minutes)}"><small>${a.enabled ? 'Activa' : 'Apagada'}</small></label>
      </div>
      <div class="settings-row">
        <div><h3>Estado</h3></div>
        <div class="muted">
          <div>Próximo ciclo: ${a.nextRun ? `${date(a.nextRun)} (en ${num(a.minutesToNext)} min)` : '—'}</div>
          <div>Último ciclo automático: ${a.lastRun ? date(a.lastRun) : 'todavía no corre desde que arrancó el servicio'}</div>
          ${a.lastResult ? `<div>Resultado: <span class="mono">${esc(a.lastResult)}</span></div>` : ''}
        </div>
      </div>
      <div class="save-bar"><button type="submit" class="btn btn-primary">Guardar</button></div>`;
    card.addEventListener('submit', async (e) => {
      e.preventDefault();
      try {
        await api('/reconciliation/auto', { method: 'PUT', body: { minutes: Number($('[name=minutes]', card).value) } });
        toast('Conciliación automática guardada');
        card.remove();
        paintAutoReconciliation();
      } catch (err) { toast(err.message, 'error'); }
    });
  }
  new MutationObserver(() => { if ($('#settings-form') && !$('#auto-recon')) paintAutoReconciliation(); })
    .observe($('#content'), { childList: true });

  // ───────── Túnel de Cloudflare ─────────
  // Instala el túnel con el token de Cloudflare (AdminTunnelController / TunnelService). La guía
  // completa está en guias/tunel.md: si cambia algo de esta pantalla, actualícela.
  const TUNNEL_STATE = {
    RUNNING: ['Funcionando', 'badge-ok'],
    START_PENDING: ['Iniciando', 'badge-info'],
    STOPPED: ['Detenido', 'badge-danger'],
    STOP_PENDING: ['Deteniéndose', 'badge-warn'],
    NO_INSTALADO: ['No instalado', ''],
    DESCONOCIDO: ['Desconocido', 'badge-warn'],
  };

  async function renderTunnel() {
    const c = $('#content');
    c.innerHTML = '<section class="card"><div class="card-pad muted">Cargando estado del túnel…</div></section>';
    paintTunnel(await api('/tunnel'));
  }

  function paintTunnel(t) {
    const [stateLabel, stateCls] = TUNNEL_STATE[t.state] || [t.state, ''];
    $('#content').innerHTML = `
      <section class="card" id="tunnel-card">
        <div class="card-head"><div><h2>Túnel de Cloudflare</h2>
          <p class="muted">Publica en internet, con HTTPS válido, solo los webhooks de este servicio para que Shopify pueda enviarlos. El panel de administración no queda expuesto.</p></div></div>
        <div class="settings-row">
          <div><h3>Estado</h3><p>Servicio de Windows <span class="mono">${esc(t.serviceId)}</span> con cloudflared ${esc(t.cloudflaredVersion)}.</p></div>
          <div class="field">
            <span><span class="badge ${stateCls}">${esc(stateLabel)}</span></span>
            ${t.tunnelId ? `<small>Túnel: <span class="mono">${esc(t.tunnelId)}</span></small>` : ''}
          </div>
        </div>
        ${t.available ? '' : `<div class="card-pad"><span class="badge badge-warn">No disponible</span> <span class="muted">${esc(t.unavailableReason)}</span></div>`}
        <form id="tunnel-form">
          <div class="settings-row">
            <div><h3>Token del túnel</h3><p>En Cloudflare: <em>Zero Trust → Networks → Tunnels</em>, abra el túnel y copie el comando de instalación de Windows. Puede pegar el comando entero o solo el token (empieza por <span class="mono">eyJ</span>).</p></div>
            <label class="field"><textarea name="token" rows="3" autocomplete="off" spellcheck="false" placeholder="eyJhIjoi…" ${t.available ? '' : 'disabled'}></textarea>
              <small>El token se guarda en la carpeta config (solo administradores). No se muestra ni se registra.</small></label>
          </div>
          <details class="card-pad steps" open>
            <summary><strong>Qué configurar en Cloudflare (Public Hostname)</strong></summary>
            <ol>
              <li><em>Subdomain</em> y <em>Domain</em>: la dirección pública, por ejemplo <span class="mono">shopify.midominio.com</span>.</li>
              <li><em>Path</em>: <span class="mono">^/webhook</span> (solo los webhooks; nada más del servicio queda publicado).</li>
              <li><em>Service</em>: tipo <strong>HTTPS</strong>, URL <span class="mono">localhost:${esc(t.apiPort)}</span> <button class="btn btn-sm" type="button" data-copy="localhost:${esc(t.apiPort)}">Copiar</button></li>
              <li><em>Additional application settings → TLS</em>: active <strong>No TLS Verify</strong> (el certificado local del servicio no es público).</li>
              <li>Después escriba <span class="mono">https://shopify.midominio.com</span> en <a href="#/webhooks">Webhooks → URL pública del servicio</a>.</li>
            </ol>
            <p class="muted">Paso a paso completo y solución de problemas:<a href="#/guias" data-guide="tunel">guía del túnel</a>.</p>
          </details>
          <div class="save-bar">
            ${t.installed ? '<button type="button" class="btn btn-danger" id="tunnel-uninstall">Desinstalar túnel</button>' : ''}
            <button type="submit" class="btn btn-primary" ${t.available ? '' : 'disabled'}>${t.installed ? 'Reinstalar con este token' : 'Instalar túnel'}</button>
          </div>
        </form>
      </section>`;

    const form = $('#tunnel-form');
    $$('[data-copy]', form).forEach((b) => b.addEventListener('click', async () => {
      try { await navigator.clipboard.writeText(b.dataset.copy); toast('Copiado'); } catch (e) { toast('No se pudo copiar: selecciónelo y cópielo a mano', 'error'); }
    }));
    bindGuideLinks(form);
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const token = $('[name=token]', form).value.trim();
      if (!token) { toast('Pegue el token del túnel', 'error'); return; }
      const ok = await confirmDialog('Instalar el túnel',
        'Se descargará cloudflared ' + t.cloudflaredVersion + ' de GitHub (unos 55 MB), se verificará su huella y se instalará el servicio de Windows ' + t.serviceId + '.' +
        (t.installed ? ' El túnel actual se reemplaza.' : ''), 'Instalar');
      if (!ok) return;
      try { startJobPolling(await api('/tunnel/install', { method: 'POST', body: { token } })); $('[name=token]', form).value = ''; } catch (err) { toast(err.message, 'error'); }
    });
    const un = $('#tunnel-uninstall');
    if (un) un.addEventListener('click', async () => {
      if (!(await confirmDialog('Desinstalar el túnel', 'Se detiene y se borra el servicio ' + t.serviceId + ' y el token guardado. Shopify dejará de llegar a los webhooks.', 'Desinstalar'))) return;
      try { paintTunnel(await api('/tunnel/uninstall', { method: 'POST' })); toast('Túnel desinstalado'); } catch (err) { toast(err.message, 'error'); }
    });
  }

  // ───────── Guías ─────────
  // Archivos Markdown en /admin/guias/ (src/main/resources/static/admin/guias). Son la única copia:
  // docs/README.md del repositorio enlaza a ellos.
  const GUIDES = [
    { key: 'credenciales-shopify', title: 'Credenciales y permisos de Shopify' },
    { key: 'tunel', title: 'Túnel de Cloudflare' },
    { key: 'instalador', title: 'Instalador del servicio' },
  ];

  async function renderGuides() {
    const key = GUIDES.some((g) => g.key === state.guide) ? state.guide : GUIDES[0].key;
    const c = $('#content');
    c.innerHTML = `
      <div class="tabs guide-tabs">${GUIDES.map((g) => `<button type="button" class="tab ${g.key === key ? 'active' : ''}" data-guide="${g.key}">${esc(g.title)}</button>`).join('')}</div>
      <article class="card card-pad guide" id="guide-body"><p class="muted">Cargando…</p></article>`;
    bindGuideLinks(c);
    const res = await fetch('guias/' + key + '.md', { cache: 'no-cache' });
    if (!res.ok) throw new Error('No se encontró la guía ' + key);
    const body = $('#guide-body');
    body.innerHTML = markdown(await res.text());
    bindGuideLinks(body);
  }

  /** Enlaces con data-guide abren esa guía en la vista Guías. */
  function bindGuideLinks(root) {
    $$('[data-guide]', root).forEach((a) => a.addEventListener('click', (e) => {
      e.preventDefault();
      state.guide = a.dataset.guide;
      if (location.hash === '#/guias') route(); else location.hash = '#/guias';
      window.scrollTo(0, 0);
    }));
  }

  // Markdown básico para las guías: títulos, párrafos, listas (un nivel de anidación), tablas,
  // citas, bloques de código, `código`, **negrita**, *cursiva* y enlaces. Todo el texto se escapa.
  function mdInline(text) {
    const codes = [];
    let s = esc(text).replace(/`([^`]+)`/g, (_, code) => { codes.push(code); return `\u0000${codes.length - 1}\u0000`; });
    s = s.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      .replace(/(^|[^*\w])\*([^*\s][^*]*)\*/g, '$1<em>$2</em>')
      .replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, (_, label, url) => {
        const guide = url.match(/^([\w-]+)\.md(#.*)?$/);
        if (guide) return `<a href="#/guias" data-guide="${guide[1]}">${label}</a>`;
        if (/^#\//.test(url)) return `<a href="${url}">${label}</a>`;
        if (/^https:\/\//.test(url)) return `<a href="${url}" target="_blank" rel="noopener noreferrer">${label}</a>`;
        return label;
      });
    return s.replace(/\u0000(\d+)\u0000/g, (_, i) => `<code>${codes[i]}</code>`);
  }

  function markdown(src) {
    const lines = src.replace(/\r/g, '').split('\n');
    const out = [];
    const listRe = /^(\s*)([-*]|\d+\.)\s+(.*)$/;
    const isBlockStart = (l) => /^(#{1,4}\s|```|>|\|)/.test(l) || listRe.test(l);
    let i = 0;
    while (i < lines.length) {
      const line = lines[i];
      if (!line.trim()) { i++; continue; }
      if (line.startsWith('```')) {
        const code = [];
        for (i++; i < lines.length && !lines[i].startsWith('```'); i++) code.push(lines[i]);
        i++;
        out.push(`<pre class="code-block">${esc(code.join('\n'))}</pre>`);
        continue;
      }
      const h = line.match(/^(#{1,4})\s+(.*)$/);
      if (h) { const n = Math.min(h[1].length + 1, 5); out.push(`<h${n}>${mdInline(h[2])}</h${n}>`); i++; continue; }
      if (line.startsWith('|') && /^\|?\s*:?-{3,}/.test(lines[i + 1] || '')) {
        const cells = (l) => l.trim().replace(/^\||\|$/g, '').split('|').map((x) => mdInline(x.trim()));
        const head = cells(line);
        const rows = [];
        for (i += 2; i < lines.length && lines[i].trim().startsWith('|'); i++) rows.push(cells(lines[i]));
        out.push(`<div class="table-wrap"><table><thead><tr>${head.map((x) => `<th>${x}</th>`).join('')}</tr></thead>` +
          `<tbody>${rows.map((r) => `<tr>${r.map((x) => `<td>${x}</td>`).join('')}</tr>`).join('')}</tbody></table></div>`);
        continue;
      }
      if (line.startsWith('>')) {
        const quote = [];
        for (; i < lines.length && lines[i].startsWith('>'); i++) quote.push(lines[i].replace(/^>\s?/, ''));
        out.push(`<blockquote>${markdown(quote.join('\n'))}</blockquote>`);
        continue;
      }
      if (listRe.test(line)) {
        const ordered = /^\s*\d+\./.test(line);
        const items = [];
        for (; i < lines.length; i++) {
          const l = lines[i];
          const m = l.match(listRe);
          if (m && !m[1]) { items.push({ text: m[3], sub: [] }); continue; }
          if (m && m[1] && items.length) { items[items.length - 1].sub.push({ ordered: /\d/.test(m[2]), text: m[3] }); continue; }
          if (l.trim() && /^\s+/.test(l) && items.length) { items[items.length - 1].text += ' ' + l.trim(); continue; }
          if (!l.trim() && listRe.test(lines[i + 1] || '') && !/^\s/.test(lines[i + 1])) continue;
          break;
        }
        const tag = ordered ? 'ol' : 'ul';
        out.push(`<${tag}>${items.map((it) => {
          const subTag = it.sub.length && it.sub[0].ordered ? 'ol' : 'ul';
          return `<li>${mdInline(it.text)}${it.sub.length ? `<${subTag}>${it.sub.map((s) => `<li>${mdInline(s.text)}</li>`).join('')}</${subTag}>` : ''}</li>`;
        }).join('')}</${tag}>`);
        continue;
      }
      const para = [line.trim()];
      for (i++; i < lines.length && lines[i].trim() && !isBlockStart(lines[i]); i++) para.push(lines[i].trim());
      out.push(`<p>${mdInline(para.join(' '))}</p>`);
    }
    return out.join('\n');
  }

  function renderAudit() {
    return simpleTable({
      key: 'audit', path: '/audit', emptyText: 'Sin actividad registrada.',
      columns: [
        { title: 'Fecha', cls: 'nowrap', render: (r) => date(r.at) },
        { title: 'Usuario', render: (r) => esc(r.username) },
        { title: 'Acción', render: (r) => `<span class="badge">${esc(r.action)}</span>` },
        { title: 'Detalle', render: (r) => `<span class="muted">${esc(r.detail)}</span>` },
      ],
    });
  }

  // ───────── Tareas en segundo plano ─────────
  function startJobPolling(job) {
    if (job) paintJob(job);
    stopJobPolling();
    state.jobTimer = setInterval(checkJob, 1200);
  }
  function stopJobPolling() { clearInterval(state.jobTimer); state.jobTimer = null; }
  async function checkJob() {
    try {
      const { job } = await api('/jobs/current');
      if (!job) { stopJobPolling(); return; }
      paintJob(job);
      if (job.running && !state.jobTimer) startJobPolling();
      if (!job.running) {
        stopJobPolling();
        const key = job.name + job.startedAt;
        if (state.lastJobKey !== key) {
          state.lastJobKey = key;
          if (!job.failed) toast(`${job.name}: ${job.message.toLowerCase()}`, job.errors.length ? 'error' : '');
          route();
        }
      }
    } catch (e) { stopJobPolling(); }
  }
  function paintJob(job) {
    const el = $('#job-toast');
    const finishedLongAgo = !job.running && job.finishedAt && Date.now() - new Date(job.finishedAt) > 60000;
    if (finishedLongAgo && !el.dataset.pinned) { el.hidden = true; return; }
    const pct = job.total ? Math.round((job.done / job.total) * 100) : 0;
    el.hidden = false;
    el.innerHTML = `<header><strong>${esc(job.name)}</strong>
        ${job.running ? `<span class="muted">${job.total ? pct + '%' : ''}</span>` : '<button class="btn btn-ghost btn-sm" data-close>Cerrar</button>'}</header>
      <div class="muted">${esc(job.message)}${job.total ? ` · ${num(job.done)} de ${num(job.total)}${job.ok !== job.done ? ` (${num(job.done - job.ok)} con error)` : ''}` : ''}</div>
      ${job.running ? `<div class="progress ${job.total ? '' : 'indeterminate'}"><span style="width:${pct}%"></span></div>` : ''}
      ${job.errors && job.errors.length ? `<ul class="errors">${job.errors.slice(0, 20).map((e) => `<li>${esc(e)}</li>`).join('')}</ul>` : ''}`;
    el.dataset.pinned = job.running || (job.errors && job.errors.length) ? '1' : '';
    $('[data-close]', el)?.addEventListener('click', () => { el.hidden = true; el.dataset.pinned = ''; });
  }

  // ───────── Inicio ─────────
  paintThemeButtons();
  matchMedia('(prefers-color-scheme: dark)').addEventListener('change', paintThemeButtons);
  (async () => {
    try {
      const me = await api('/auth/me');
      state.user = me.username;
      me.mustChangePassword ? showForcePassword() : showApp();
    } catch (e) {
      if ($('#app').hidden && $('#force-password-view').hidden) showLogin();
    }
  })();
})();
