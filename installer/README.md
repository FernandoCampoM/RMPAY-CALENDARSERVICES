# Instalador de RMPAY Service

Genera `installer/Output/RMPAYService-Setup-<versión>.exe`: un asistente de Windows que pide la
configuración (SQL Server, Retail Manager, Shopify, puertos), instala la aplicación como servicio de
Windows (`RMPAYService`) y crea en el escritorio el acceso al panel de administración local.

Requisitos del equipo del cliente: Windows 10/11 o Windows Server 2016+ de 64 bits. No necesita Java.

## Archivos

| Archivo | Qué es |
|---|---|
| `RMPAYService.iss` | Script de Inno Setup: páginas del asistente, validaciones, servicio, firewall, accesos directos |
| `service/RMPAYService.xml` | Configuración del servicio (WinSW) |
| `build.ps1` | Compila el jar (con tests), arma el runtime de Java con jlink y compila el instalador |
| `tools/WinSW-x64.exe` | WinSW 2.12 (no se sube a git; lo descarga quien compila) |

## Compilar a mano

Requisitos: JDK 20+ (`JAVA_HOME`), Inno Setup 6 y `tools/WinSW-x64.exe`
([descarga](https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-x64.exe),
SHA256 `05B82D46AD331CC16BDC00DE5C6332C1EF818DF8CEEFCD49C726553209B3A0DA`).

```powershell
powershell -ExecutionPolicy Bypass -File installer\build.ps1 [-Version 1.0.0]
```

## Verificación automática en cada cambio

- **Antes de cada `git push`** (hook local `.githooks/pre-push`): corre los tests y recompila el
  instalador; si algo falla, el push se cancela. Se activa una vez por copia del repositorio:
  `git config core.hooksPath .githooks`. Para saltarlo en un caso puntual: `git push --no-verify`.
- **En GitHub** (`.github/workflows/installer.yml`): en cada push y pull request corre lo mismo en
  Windows y deja el `.exe` para descargar en la pestaña *Actions* (artefacto `RMPAYService-Setup`,
  30 días). La versión lleva el número de ejecución, por ejemplo `0.0.1.42`.

## Configuración en desarrollo

Las contraseñas y secretos no están en el jar. En desarrollo van en `config/application.properties`
en la raíz del proyecto (fuera de git); en los clientes ese archivo lo escribe el instalador.
