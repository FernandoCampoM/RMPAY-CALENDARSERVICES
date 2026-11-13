# Instalador del servicio

`RMPAYService-Setup-<versión>.exe` es un asistente de Windows (siguiente, siguiente) que pide la
configuración, instala RMPAY Service como **servicio de Windows** y crea en el escritorio el acceso
**Panel RMPAY** al panel de administración local.

> Regla del proyecto: cualquier cambio en el instalador (páginas, campos, carpetas, servicio)
> actualiza esta guía en el mismo commit.

## Requisitos del equipo

- Windows 10/11 o Windows Server 2016 o posterior, de 64 bits. No necesita Java (va incluido).
- Permisos de administrador para instalar.
- SQL Server con la base de datos de Retail Manager (por ejemplo `localhost\CSE`, base `POSDPS`).
- RM API funcionando (por ejemplo `http://localhost:8180`).

## Dónde conseguir el instalador

- **GitHub** → repositorio → pestaña *Actions* → última ejecución en verde → artefacto
  `RMPAYService-Setup` (se guarda 30 días).
- O compilándolo: `powershell -ExecutionPolicy Bypass -File installer\build.ps1` deja el `.exe`
  en `installer\Output\` (ver `installer/README.md`).

## Páginas del asistente

| Página | Campo | De dónde sale |
|---|---|---|
| Base de datos | Servidor SQL | El de Retail Manager, por ejemplo `localhost\CSE` o `192.168.1.10,1433` |
| | Usuario y contraseña | Usuario de SQL Server con acceso a la base de RM (por ejemplo `sa`) |
| | Base de datos de Retail Manager | Normalmente `POSDPS` |
| | Base de datos del servicio | Se crea sola si no existe (por ejemplo `ShopifyServiceDB`) |
| Retail Manager | Dirección de RM API | Donde corre RM API, por ejemplo `http://localhost:8180` |
| | Productos de envío y propina | Códigos en RM (`FLETE`, `PROPINA`); si no existen, el servicio los crea |
| Shopify | Tienda, Client ID, Client secret, versión de la API | Ver [credenciales y permisos de Shopify](credenciales-shopify.md) |
| | Secreto de webhooks (opcional) | Se puede dejar vacío y ponerlo luego en el panel → *Webhooks* |
| Puertos | Puerto de la API | `9192` por defecto; es el que se publica con el túnel |
| | Puerto del panel | `47821` por defecto; solo local |
| Opciones | Modo simulación | Arma las facturas sin enviarlas a RM (para probar) |
| | Regla del firewall | Permite llamadas a la API desde otros equipos de la red |

Al volver a ejecutar el instalador sobre una instalación existente, los campos aparecen con los
valores actuales: sirve para cambiar una credencial sin perder el resto.

## Qué deja instalado

| Qué | Dónde |
|---|---|
| Aplicación y Java | `C:\Program Files\RMPAY Service` (o la carpeta elegida): `app\`, `runtime\` |
| Configuración (con contraseñas) | `config\application.properties`, solo legible por Administradores y SYSTEM |
| Servicio de Windows | **RMPAY Service** (`RMPAYService`), inicio automático, reinicio si falla |
| Registros | `logs\RMPAYService.out.log` |
| Acceso directo | Escritorio: *Panel RMPAY* → `https://localhost:<puerto del panel>/admin` |

## Primer ingreso al panel

1. Abra *Panel RMPAY* del escritorio. El navegador avisará del certificado local: es esperado
   (el panel solo responde en este equipo); acepte para continuar.
2. Usuario `admin`, contraseña `admin`. El panel obliga a cambiarla en el primer ingreso.

## Después de instalar

1. Revise el panel → *Panel* y *Configuración*.
2. Para recibir pedidos por webhooks: [instale el túnel](tunel.md) y configure el panel →
   *Webhooks*.

## Desinstalar

1. Si instaló el túnel, desinstálelo antes desde el panel → *Túnel*.
2. *Configuración de Windows → Aplicaciones → RMPAY Service → Desinstalar*. Al final pregunta si
   borrar también la configuración y los registros.
