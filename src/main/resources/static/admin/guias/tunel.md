# Túnel de Cloudflare

Shopify solo envía webhooks a una dirección **HTTPS pública con certificado válido**. El servicio
corre en un equipo local con un certificado propio, así que necesita un túnel: Cloudflare recibe
las llamadas de Shopify en un dominio público (por ejemplo `shopify.midominio.com`) y las pasa al
servicio en el equipo, sin abrir puertos en el router.

El panel instala el túnel con un botón. Solo hace falta el **token** del túnel que entrega
Cloudflare.

> Regla del proyecto: cualquier cambio en la instalación del túnel (pantalla *Túnel*,
> `TunnelService`, versión de cloudflared) actualiza esta guía en el mismo commit.

## Requisitos

- Una cuenta de Cloudflare con el **dominio** que se va a usar (por ejemplo `midominio.com`)
  agregado como zona.
- RMPAY Service instalado con el instalador en el equipo (el botón no funciona en un equipo de
  desarrollo).
- Salida a internet desde el equipo hacia GitHub (para descargar cloudflared) y hacia Cloudflare.

## 1. Crear el túnel en Cloudflare

1. Entre a `one.dash.cloudflare.com` (*Zero Trust*).
2. Vaya a *Networks → Tunnels* (en algunas cuentas: *Networks → Connectors → Cloudflare Tunnels*)
   y pulse *Create a tunnel*.
3. Tipo: **Cloudflared**. Nombre: por ejemplo `rmpay-<cliente>`. Guardar.
4. En *Install and run a connector* elija **Windows**. Cloudflare muestra un comando como:
   `cloudflared.exe service install eyJhIjoi…`
   Copie ese comando completo (o solo la parte que empieza por `eyJ`, que es el token).
   **No** ejecute el comando: el panel lo hace por usted.

## 2. Publicar solo los webhooks (Public Hostname)

En el mismo asistente, paso *Route traffic* (en cuentas nuevas se llama *Published application
routes*; en túneles ya creados: abrir el túnel → *Public Hostname → Add a public hostname*):

| Campo | Valor |
|---|---|
| Subdomain | por ejemplo `shopify` |
| Domain | el dominio de la cuenta, por ejemplo `midominio.com` |
| Path | `^/webhook` |
| Service → Type | `HTTPS` |
| Service → URL | `localhost:9192` (el puerto de la API; el panel muestra el correcto) |
| Additional application settings → TLS → **No TLS Verify** | Activado |

- El **Path** hace que solo `/webhook…` quede publicado. El panel de administración y el resto del
  servicio no son accesibles desde internet.
- **No TLS Verify** es necesario porque el certificado local del servicio no es público.
- Cloudflare crea solo el registro DNS (`CNAME` hacia `<id>.cfargotunnel.com`). Si ya existía un
  registro con ese nombre, bórrelo antes en *DNS → Records* o Cloudflare no podrá crear la ruta.

## 3. Instalar el túnel desde el panel

1. Abra el panel (acceso directo *Panel RMPAY* del escritorio) → **Túnel**.
2. Pegue el comando o el token en *Token del túnel* y pulse **Instalar túnel**.
3. El servicio:
   - descarga cloudflared (versión fija) de GitHub y comprueba su huella SHA256; si no coincide,
     no instala nada;
   - guarda el token en `config\tunnel-token.txt` (carpeta que solo leen Administradores y
     SYSTEM);
   - registra e inicia el servicio de Windows **RMPAYTunnel** (*RMPAY Tunnel (Cloudflare)*), con
     inicio automático y reinicio si falla.
4. El estado debe quedar en **Funcionando** y, en Cloudflare, el túnel en **Healthy**.

Para cambiar de túnel o de token, pegue el nuevo y pulse **Reinstalar con este token**.
**Desinstalar túnel** detiene y borra el servicio y el token.

## 4. Conectar los webhooks

1. Panel → **Webhooks** → *URL pública del servicio*: `https://shopify.midominio.com` → Guardar.
2. Siga ahí los pasos para crear cada webhook en Shopify y pegar la llave de firma
   (ver también [credenciales y permisos de Shopify](credenciales-shopify.md)).

## 5. Comprobar que funciona

1. Desde cualquier navegador abra `https://shopify.midominio.com/webhook/orders`. Debe responder
   el servicio con un error de método (por ejemplo *405*), **no** una página de error de
   Cloudflare. Shopify usa POST, por eso el navegador no recibe un pedido.
2. En Shopify use *Enviar notificación de prueba* con el webhook en *Solo registrar* y compruebe
   que aparece en el panel → *Webhooks* con la firma válida.

## Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| Botón deshabilitado: *Solo funciona en el servicio instalado…* | El panel no corre desde la instalación del instalador | Use el panel del equipo donde está instalado el servicio |
| *La descarga de cloudflared no coincide con la huella esperada* | Descarga corrupta o interceptada por un proxy | Reintente; si sigue, revise el proxy o antivirus del equipo |
| *No se pudo descargar cloudflared de GitHub* | Sin salida a internet hacia `github.com` | Abrir la salida a `github.com` y `objects.githubusercontent.com` |
| Error **1033** de Cloudflare | El túnel no está conectado | Panel → Túnel: ver estado; en Windows, servicio *RMPAY Tunnel (Cloudflare)*; registros en `tunnel\logs` |
| Error **502** de Cloudflare | Cloudflare llega al equipo pero no al servicio | Revisar que la URL sea `HTTPS` `localhost:<puerto de la API>` y que *No TLS Verify* esté activo; que RMPAY Service esté corriendo |
| Error **404** en `/webhook/orders` | Falta o sobra algo en *Path* | Path debe ser `^/webhook` |
| La ruta no se crea: *record already exists* | Ya hay un registro DNS con ese nombre | Borrarlo en *DNS → Records* y volver a guardar la ruta |

## Detalles técnicos

- Versión de cloudflared: **2026.10.7**, descargada de
  `github.com/cloudflare/cloudflared/releases`, SHA256
  `2493dbc168923bb3e59e5e7716f71deced92992eea9acd6e9d74f84ac7751343`. Para cambiarla se
  actualizan `TunnelService.CLOUDFLARED_VERSION` y `CLOUDFLARED_SHA256` (y esta guía).
- Archivos en la carpeta de instalación: `tunnel\cloudflared.exe`, `tunnel\RMPAYTunnel.exe`
  (WinSW, copia de `RMPAYService.exe`), `tunnel\RMPAYTunnel.xml`, `tunnel\logs\` y
  `config\tunnel-token.txt`.
- cloudflared lee el token con `--token-file`: el token no aparece en la línea de comandos del
  servicio ni en los registros.
- Se usa un servicio propio (`RMPAYTunnel`) y no el `Cloudflared` estándar para no chocar con
  otros túneles instalados en el mismo equipo.
- Al desinstalar RMPAY Service, desinstale antes el túnel desde el panel (el desinstalador todavía
  no lo quita).

### Método manual (sin el panel)

Para equipos sin el servicio instalado o túneles administrados por línea de comandos
(`cloudflared tunnel login`, `config.yml` con reglas `ingress`): el `cert.pem` de
`cloudflared tunnel login` vale para **una sola zona**; si se trabaja con varios dominios, use
`--origincert` con el certificado de la zona correcta, o el registro DNS se crea en otra zona. El
método del panel con token evita ese problema.
