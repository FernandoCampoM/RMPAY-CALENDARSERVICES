// Convierte las guías del panel (src/main/resources/static/admin/guias/*.md) en páginas HTML
// listas para imprimir a PDF (las usa docs/build-pdfs.ps1).
// Usa el mismo renderizador Markdown del panel (funciones mdInline y markdown de admin.js), así el
// PDF y la vista Guías muestran exactamente lo mismo. Uso: node docs/render-guides.js <carpeta-salida>
'use strict';
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..');
const guidesDir = path.join(root, 'src/main/resources/static/admin/guias');
const adminJs = fs.readFileSync(path.join(root, 'src/main/resources/static/admin/admin.js'), 'utf8');
const outDir = path.resolve(process.argv[2] || path.join(root, 'target/guias-html'));

// Extrae de admin.js el bloque desde "function mdInline" hasta antes de la siguiente función del panel
const start = adminJs.indexOf('  function mdInline(');
const end = adminJs.indexOf('  function renderAudit(');
if (start < 0 || end < start) {
  throw new Error('No se encontró el renderizador Markdown en admin.js (mdInline ... renderAudit)');
}
const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
// eslint-disable-next-line no-new-func
const markdown = new Function('esc', adminJs.slice(start, end) + '\nreturn markdown;')(esc);

const TITLES = {
  'credenciales-shopify': 'Credenciales y permisos de Shopify',
  tunel: 'Túnel de Cloudflare',
  instalador: 'Instalador del servicio',
};

const css = `
  @page { size: A4; margin: 18mm 16mm; }
  body { font-family: "Segoe UI", Arial, sans-serif; font-size: 10.5pt; line-height: 1.5; color: #1f2328; }
  h2 { font-size: 20pt; margin: 0 0 10px; color: #0b1f44; }
  h3 { font-size: 14pt; margin: 22px 0 6px; padding-top: 10px; border-top: 1px solid #d0d7de; color: #0b1f44; page-break-after: avoid; }
  h4, h5 { font-size: 11.5pt; margin: 14px 0 4px; page-break-after: avoid; }
  p, ul, ol, table, pre, blockquote { margin: 0 0 9px; }
  ul, ol { padding-left: 20px; }
  code { font-family: Consolas, monospace; font-size: 9.5pt; background: #f3f4f6; border: 1px solid #e5e7eb; border-radius: 3px; padding: 0 3px; }
  pre { font-family: Consolas, monospace; font-size: 9pt; background: #f3f4f6; border: 1px solid #e5e7eb; border-radius: 4px; padding: 8px 10px; white-space: pre-wrap; word-break: break-all; }
  table { border-collapse: collapse; width: 100%; font-size: 9.5pt; page-break-inside: auto; }
  tr { page-break-inside: avoid; }
  th, td { border: 1px solid #d0d7de; padding: 4px 6px; text-align: left; vertical-align: top; }
  th { background: #eef2f7; }
  blockquote { border-left: 3px solid #2f6fed; background: #eef4ff; padding: 6px 10px; }
  a { color: #2f6fed; text-decoration: none; }
  .foot { margin-top: 24px; font-size: 8.5pt; color: #6b7280; border-top: 1px solid #d0d7de; padding-top: 6px; }
`;

fs.mkdirSync(outDir, { recursive: true });
const generated = [];
for (const file of fs.readdirSync(guidesDir).filter((f) => f.endsWith('.md')).sort()) {
  const key = file.replace(/\.md$/, '');
  const source = fs.readFileSync(path.join(guidesDir, file), 'utf8');
  // Los enlaces entre guías (data-guide) se convierten en referencias al PDF correspondiente
  const body = markdown(source).replace(/<a href="#\/guias" data-guide="([\w-]+)">/g, '<a href="$1.pdf">');
  const html = `<!doctype html><html lang="es"><head><meta charset="utf-8"><title>${esc(TITLES[key] || key)}</title>`
    + `<style>${css}</style></head><body>${body}`
    + `<p class="foot">RMPAY Service · ${esc(TITLES[key] || key)} · generado de src/main/resources/static/admin/guias/${esc(file)}. `
    + `La versión al día está en el panel de administración → Guías.</p></body></html>`;
  fs.writeFileSync(path.join(outDir, key + '.html'), html, 'utf8');
  generated.push(key);
}
console.log(generated.join('\n'));
