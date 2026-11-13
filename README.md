# RMPAY Service

Servicio Spring Boot de RMPAY que:

- ofrece la **API de calendario** del dashboard de RMPAY ([documentación en Postman](https://documenter.getpostman.com/view/19269592/2sB3QKq9CR));
- **sincroniza Shopify con Retail Manager (RM)**: catálogo de productos, existencias y facturación de los pedidos en RM;
- trae un **panel de administración local** para configurar y vigilar la sincronización;
- se instala en Windows como **servicio** con un instalador (siguiente, siguiente).

## Tabla de contenidos

- [Descargar e instalar](#descargar-e-instalar)
- [Guías](#guías)
- [Qué hace la integración con Shopify](#qué-hace-la-integración-con-shopify)
- [Panel de administración](#panel-de-administración)
- [Desarrollo](#desarrollo)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Despliegue en Docker (API de calendario)](#despliegue-en-docker-api-de-calendario)

## Descargar e instalar

1. Descargue `RMPAYService-Setup-<versión>.exe` desde
   [Releases](https://github.com/FernandoCampoM/RMPAY-CALENDARSERVICES/releases). Cada push también
   deja el instalador en *Actions → (ejecución) → Artifacts → `RMPAYService-Setup`* (30 días).
2. Ejecútelo como administrador en Windows 10/11 o Windows Server 2016+ de 64 bits (no necesita Java).
3. El asistente pide SQL Server, RM API, las credenciales de Shopify y los puertos; instala el
   servicio **RMPAY Service** y deja en el escritorio el acceso **Panel RMPAY**.
4. Entre al panel con `admin` / `admin`; pide cambiar la contraseña en el primer ingreso.

Paso a paso completo: [guía del instalador](docs/pdf/instalador.pdf).

## Guías

Están en el panel (vista **Guías**) y en PDF en [`docs/pdf`](docs/pdf):

| Guía | PDF |
|---|---|
| Credenciales y permisos de Shopify (tienda, Client ID/secret, alcances, datos protegidos, errores frecuentes) | [credenciales-shopify.pdf](docs/pdf/credenciales-shopify.pdf) |
| Túnel de Cloudflare para recibir los webhooks de Shopify | [tunel.pdf](docs/pdf/tunel.pdf) |
| Instalador del servicio | [instalador.pdf](docs/pdf/instalador.pdf) |

Más detalle en [`docs/README.md`](docs/README.md).

## Qué hace la integración con Shopify

| Área | Comportamiento |
|---|---|
| Productos | Ciclo cada 5 minutos: altas, precios, descripción, existencias (siempre de RM a Shopify) y publicación. Quién manda en precio, descripción y altas se elige en el panel: RM, Shopify o ninguno. |
| Pedidos | Ciclo cada 5 minutos, independiente del de productos: los pedidos pagados se facturan en RM (`ImportExternalInvoice`) con líneas de envío (`FLETE`) y propina (`PROPINA`), que el servicio crea en RM si faltan. |
| Webhooks | `orders/paid`, `orders/cancelled` y `refunds/create`, con firma HMAC. Cada llamada queda registrada (body, factura enviada, resultado, error) y se ve en el panel. |
| Número de factura en RM | `K` + número del pedido si la envía el ciclo, `KW` + número si llega por el webhook. El pedido se reserva por su ID de Shopify antes de enviarlo: nunca se factura dos veces. |
| Errores | Las facturas que RM rechaza se reintentan solas (hasta 10 veces) y quedan en el panel. Si Shopify niega la consulta de pedidos, la fecha de sincronización no avanza. |
| Conciliación | Empareja los catálogos por código, código de barras y nombre. Se puede aplicar a mano o automáticamente cada N minutos (Configuración, 0 = apagada). Al crear en RM se usa el SKU exacto de Shopify. |

## Panel de administración

Solo responde en el equipo donde está instalado: `https://localhost:47821/admin` por defecto.

| Vista | Para qué |
|---|---|
| Panel | Resumen: productos vinculados, pendientes de conciliar, pedidos procesados |
| Conciliación | Emparejar Shopify con RM: vincular, asignar, crear en RM o en Shopify |
| Configuración | Quién manda, borradores, límite de borrado, conciliación automática, contraseña |
| Vinculados / Pedidos | Productos vinculados; pedidos procesados con su factura en RM (K… / KW…) |
| Facturas con error | Reintentos pendientes y vista previa de la factura |
| Webhooks | Traza de lo recibido, URL pública, llave de firma e instrucciones para Shopify |
| Túnel | Instala el túnel de Cloudflare con el token del túnel (servicio `RMPAYTunnel`) |
| Guías / Auditoría | Las guías del proyecto y el registro de cambios hechos en el panel |

## Desarrollo

Requisitos: JDK 20+, Maven (o `mvnw`), SQL Server con las bases `POSDPS` (Retail Manager) y
`ShopifyServiceDB` (la crea el servicio), y RM API (por ejemplo `http://localhost:8180`).

- **Configuración:** `src/main/resources/application.properties` no lleva contraseñas ni secretos
  (usa `${…}`). En desarrollo van en `config/application.properties` en la raíz del proyecto, que
  está fuera de git; en los clientes ese archivo lo escribe el instalador.
- **Puertos por defecto:** API `9192` (HTTPS), panel `47821` (HTTPS, solo local).
- **Ejecutar:** `./mvnw spring-boot:run`
- **Tests:** `./mvnw test`
- **Instalador:** `powershell -ExecutionPolicy Bypass -File installer\build.ps1` (ver
  [`installer/README.md`](installer/README.md)).
- **Hooks de git** (activar una vez: `git config core.hooksPath .githooks`):
  - `pre-commit`: si cambian las guías, regenera los PDF de `docs/pdf`;
  - `pre-push`: corre los tests y recompila el instalador; avisa si cambió el instalador, el túnel
    o Shopify sin actualizar las guías.
- **GitHub Actions** (`.github/workflows/installer.yml`): compila el instalador en cada push y pull request.

## Estructura del proyecto

| Carpeta | Contenido |
|---|---|
| `src/main/java/.../services/services/Shopify` | Cliente GraphQL, sincronización de productos y pedidos, facturación y webhooks |
| `src/main/java/.../services/services/pos` | Cliente de RM API (productos, facturas, FLETE/PROPINA) |
| `src/main/java/.../admin` | Panel de administración: controladores, conciliación, túnel, conciliación automática |
| `src/main/java/.../backgroundRoutines` | Ciclos de productos y pedidos |
| `src/main/resources/static/admin` | Panel web (HTML, JS, CSS) y guías en Markdown (`guias/`) |
| `installer/` | Instalador de Windows (Inno Setup, WinSW, Java con jlink) |
| `docs/` | Índice de guías, PDF y generador de PDF |
| `.githooks/`, `.github/workflows/` | Verificación automática antes de cada commit/push y en GitHub |

## Despliegue en Docker (API de calendario)

> ⚠️ **Advertencia**
> Antes de desplegar en el servidor de producción, asegúrese de que:
> - No se eliminen las carpetas `C:\RMPAYService\images` y `C:\RMPAYService\sql_data`: tienen la
>   información de la base de datos y las imágenes subidas por los usuarios en sus perfiles y negocios.

### 1. Crear la imagen Docker

```bash
mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=rmpaycalendarService
```

### 2. Guardar la imagen

```bash
docker save rmpaycalendarService > D:\\Images\\rmpaycalendarService.tar
```

Transfiera el archivo al servidor, a la carpeta `C:\\RmpaycalendarService`.

### 3. Cargar y desplegar la nueva imagen en el servidor

1. Detenga el contenedor en ejecución (desde Docker Desktop o una terminal):
   ```bash
   docker stop <nombre-del-contenedor>
   ```
2. Cargue la imagen:
   ```bash
   docker load -i C:\\RmpaycalendarService\\rmpaycalendarService.tar
   ```
3. Recree los contenedores con docker-compose:
   ```bash
   docker compose up -d
   ```

Asegúrese de que `docker-compose.yml` esté configurado para la imagen `rmpaycalendarService`.
