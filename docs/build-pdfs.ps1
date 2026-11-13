# Genera docs/pdf/<guía>.pdf a partir de las guías del panel (src/main/resources/static/admin/guias).
# Requisitos: Node.js y Microsoft Edge o Google Chrome (los trae Windows / el runner de GitHub).
# Uso: powershell -ExecutionPolicy Bypass -File docs\build-pdfs.ps1
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$htmlDir = Join-Path $root 'target\guias-html'
$pdfDir = Join-Path $root 'docs\pdf'

$browser = @(
    "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe",
    "$env:ProgramFiles\Microsoft\Edge\Application\msedge.exe",
    "$env:ProgramFiles\Google\Chrome\Application\chrome.exe"
) | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
if (-not $browser) { throw 'No se encontró Microsoft Edge ni Google Chrome para generar los PDF' }

$keys = & node (Join-Path $PSScriptRoot 'render-guides.js') $htmlDir
if ($LASTEXITCODE -ne 0) { throw 'No se pudieron convertir las guías a HTML' }

New-Item -ItemType Directory -Force $pdfDir | Out-Null
$profileDir = Join-Path $env:TEMP ('rmpay-pdf-' + [guid]::NewGuid())
try {
    foreach ($key in $keys) {
        $html = Join-Path $htmlDir "$key.html"
        $pdf = Join-Path $pdfDir "$key.pdf"
        $url = 'file:///' + ($html -replace '\\', '/')
        $proc = Start-Process -FilePath $browser -Wait -PassThru -WindowStyle Hidden -ArgumentList @(
            '--headless=new', '--disable-gpu', '--no-pdf-header-footer', "--user-data-dir=`"$profileDir`"",
            "--print-to-pdf=`"$pdf`"", "`"$url`"")
        if (-not (Test-Path $pdf)) { throw "No se generó $pdf (código $($proc.ExitCode))" }
        Write-Host "-- $pdf"
    }
} finally {
    Remove-Item -Recurse -Force $profileDir -ErrorAction SilentlyContinue
}
