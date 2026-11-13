<#
  Genera el instalador de RMPAY Service: installer\Output\RMPAYService-Setup-<versión>.exe

  Pasos: compila el jar con Maven (corre los tests), arma un runtime de Java recortado con
  jlink, junta jar + runtime + WinSW en installer\build\stage y compila RMPAYService.iss con
  Inno Setup.

  Requisitos en la máquina que compila:
    - JDK 20 o superior (JAVA_HOME o -JdkHome) con jlink
    - Inno Setup 6 (ISCC.exe)
    - installer\tools\WinSW-x64.exe (WinSW 2.12: https://github.com/winsw/winsw/releases)

  Uso:  powershell -ExecutionPolicy Bypass -File installer\build.ps1 [-Version 1.0.0] [-BuildNumber 42] [-SkipTests]
        -BuildNumber se agrega como cuarto número de la versión (lo usa GitHub Actions).
#>
param(
    [string]$Version = '',
    [string]$BuildNumber = '',
    [string]$JdkHome = $env:JAVA_HOME,
    [switch]$SkipTests
)

$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$build = Join-Path $PSScriptRoot 'build'
$stage = Join-Path $build 'stage'
$output = Join-Path $PSScriptRoot 'Output'
$winsw = Join-Path $PSScriptRoot 'tools\WinSW-x64.exe'

# Módulos de Java que usa la aplicación (Spring Boot, Tomcat, Hibernate, mssql-jdbc, RabbitMQ)
$modules = @(
    'java.base', 'java.compiler', 'java.desktop', 'java.instrument', 'java.logging',
    'java.management', 'java.management.rmi', 'java.naming', 'java.net.http', 'java.prefs',
    'java.rmi', 'java.scripting', 'java.security.jgss', 'java.security.sasl', 'java.sql',
    'java.sql.rowset', 'java.transaction.xa', 'java.xml', 'java.xml.crypto',
    'jdk.charsets', 'jdk.crypto.cryptoki', 'jdk.crypto.ec', 'jdk.localedata', 'jdk.management',
    'jdk.naming.dns', 'jdk.net', 'jdk.unsupported', 'jdk.zipfs'
) -join ','

function Find-Iscc {
    $candidates = @(
        (Join-Path $env:LOCALAPPDATA 'Programs\Inno Setup 6\ISCC.exe'),
        (Join-Path ${env:ProgramFiles(x86)} 'Inno Setup 6\ISCC.exe'),
        (Join-Path $env:ProgramFiles 'Inno Setup 6\ISCC.exe')
    )
    foreach ($c in $candidates) { if ($c -and (Test-Path $c)) { return $c } }
    $cmd = Get-Command ISCC.exe -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    throw 'No se encontró Inno Setup 6 (ISCC.exe). Instálelo desde https://jrsoftware.org/isdl.php'
}

# ── Verificaciones ──
if (-not $JdkHome -or -not (Test-Path (Join-Path $JdkHome 'bin\jlink.exe'))) {
    throw 'No se encontró jlink. Indique el JDK con -JdkHome o la variable JAVA_HOME.'
}
if (-not (Test-Path $winsw)) {
    throw "Falta $winsw (WinSW 2.12 x64: https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-x64.exe)"
}
$iscc = Find-Iscc

if (-not $Version) {
    [xml]$pom = Get-Content (Join-Path $root 'pom.xml')
    $Version = $pom.project.version -replace '-SNAPSHOT', ''
}
if ($BuildNumber) { $Version = "$Version.$BuildNumber" }
Write-Host "== RMPAY Service $Version =="

# ── 1. Jar ──
Write-Host '-- Compilando el jar (Maven)'
$mvnArgs = @('-q', '-f', (Join-Path $root 'pom.xml'), 'package')
if ($SkipTests) { $mvnArgs += '-DskipTests' }
& (Join-Path $root 'mvnw.cmd') @mvnArgs
if ($LASTEXITCODE -ne 0) { throw "Maven terminó con código $LASTEXITCODE" }
$jar = Join-Path $root 'target\RMPAYservice.jar'
if (-not (Test-Path $jar)) { throw "No se generó $jar" }

# ── 2. Carpeta de preparación ──
if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
New-Item -ItemType Directory -Force (Join-Path $stage 'app') | Out-Null
Copy-Item $jar (Join-Path $stage 'app\RMPAYservice.jar')
Copy-Item $winsw (Join-Path $stage 'RMPAYService.exe')
Copy-Item (Join-Path $PSScriptRoot 'service\RMPAYService.xml') (Join-Path $stage 'RMPAYService.xml')

# ── 3. Runtime de Java recortado ──
Write-Host '-- Generando el runtime de Java (jlink)'
& (Join-Path $JdkHome 'bin\jlink.exe') --add-modules $modules --include-locales=en,es `
    --strip-debug --no-header-files --no-man-pages --compress=2 `
    --output (Join-Path $stage 'runtime')
if ($LASTEXITCODE -ne 0) { throw "jlink terminó con código $LASTEXITCODE" }

# ── 4. Instalador ──
Write-Host '-- Compilando el instalador (Inno Setup)'
& $iscc /Q "/DAppVersion=$Version" "/DStageDir=$stage" "/O$output" (Join-Path $PSScriptRoot 'RMPAYService.iss')
if ($LASTEXITCODE -ne 0) { throw "Inno Setup terminó con código $LASTEXITCODE" }

$exe = Join-Path $output "RMPAYService-Setup-$Version.exe"
Write-Host ("== Listo: {0} ({1:N1} MB)" -f $exe, ((Get-Item $exe).Length / 1MB))
