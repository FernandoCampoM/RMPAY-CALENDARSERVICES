# Documentación de RMPAY Service

Las guías viven en una sola copia, dentro del panel de administración, para que el panel
(vista **Guías**) y el repositorio muestren siempre lo mismo:

| Guía | Archivo | PDF |
|---|---|---|
| Credenciales y permisos de Shopify (con historial de hallazgos) | [credenciales-shopify.md](../src/main/resources/static/admin/guias/credenciales-shopify.md) | [PDF](pdf/credenciales-shopify.pdf) |
| Túnel de Cloudflare (instalación desde el panel) | [tunel.md](../src/main/resources/static/admin/guias/tunel.md) | [PDF](pdf/tunel.pdf) |
| Instalador del servicio | [instalador.md](../src/main/resources/static/admin/guias/instalador.md) | [PDF](pdf/instalador.pdf) |

Los PDF de `docs/pdf/` se generan de esos mismos archivos con `docs/build-pdfs.ps1` (Node y
Edge/Chrome). El hook `.githooks/pre-commit` los regenera y los agrega al commit cada vez que
cambia una guía, así que no se editan a mano.

Detalles de compilación del instalador: [installer/README.md](../installer/README.md).

## Mantener las guías al día

Todo cambio que afecte a la instalación, al túnel o a la conexión con Shopify (credenciales,
permisos, alcances, webhooks) actualiza la guía correspondiente **en el mismo commit**. Lo nuevo
que se descubra sobre cómo conseguir credenciales o permisos de Shopify se agrega al *Historial
de hallazgos* de `credenciales-shopify.md`.

El hook `.githooks/pre-push` avisa (sin cancelar el push) si cambian archivos de esas áreas sin
tocar las guías.
