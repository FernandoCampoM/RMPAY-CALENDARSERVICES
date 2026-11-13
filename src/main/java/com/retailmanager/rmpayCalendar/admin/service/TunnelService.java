package com.retailmanager.rmpayCalendar.admin.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Túnel de Cloudflare instalado desde el panel: con el token que entrega Cloudflare
 * (Zero Trust → Networks → Tunnels) descarga cloudflared (versión fija, verificada por SHA256)
 * y lo registra como servicio de Windows propio (RMPAYTunnel), usando el mismo WinSW que
 * RMPAY Service. Así no choca con otros túneles "Cloudflared" que tenga el equipo.
 *
 * Archivos (relativos a la carpeta de instalación, que es el directorio de trabajo del servicio):
 *  - tunnel\cloudflared.exe, tunnel\RMPAYTunnel.exe (copia de RMPAYService.exe) y tunnel\RMPAYTunnel.xml
 *  - config\tunnel-token.txt: el token; config\ solo la leen Administradores y SYSTEM (lo protege el instalador)
 * El token nunca va en la línea de comandos ni en los logs.
 * Guía para el usuario: static/admin/guias/tunel.md (mantenerla al día si cambia algo de aquí).
 */
@Slf4j
@Service
public class TunnelService {

    public static final String SERVICE_ID = "RMPAYTunnel";
    static final String CLOUDFLARED_VERSION = "2026.10.7";
    static final String CLOUDFLARED_URL = "https://github.com/cloudflare/cloudflared/releases/download/"
            + CLOUDFLARED_VERSION + "/cloudflared-windows-amd64.exe";
    static final String CLOUDFLARED_SHA256 = "2493dbc168923bb3e59e5e7716f71deced92992eea9acd6e9d74f84ac7751343";

    private static final Pattern TOKEN_CHARS = Pattern.compile("[A-Za-z0-9+/=_-]{40,4096}");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Cuenta y túnel de Cloudflare que contiene el token (el secreto "s" no se guarda aquí). */
    public record TokenInfo(String accountTag, String tunnelId) {
    }

    /** Resultado de un comando externo: código de salida y salida combinada. */
    record CommandResult(int exitCode, String output) {
    }

    private final Path baseDir;

    public TunnelService() {
        this(Paths.get(System.getProperty("user.dir")));
    }

    TunnelService(Path baseDir) {
        this.baseDir = baseDir.toAbsolutePath();
    }

    Path tunnelDir() {
        return baseDir.resolve("tunnel");
    }

    Path tokenFile() {
        return baseDir.resolve("config").resolve("tunnel-token.txt");
    }

    Path cloudflaredExe() {
        return tunnelDir().resolve("cloudflared.exe");
    }

    Path wrapperExe() {
        return tunnelDir().resolve(SERVICE_ID + ".exe");
    }

    /** WinSW del servicio principal (lo copia el instalador como RMPAYService.exe). */
    Path mainWrapperExe() {
        return baseDir.resolve("RMPAYService.exe");
    }

    // ───────────── Token ─────────────

    /**
     * Valida el token de Cloudflare: base64 de {"a": cuenta, "t": id del túnel, "s": secreto}.
     * Acepta también el comando completo que muestra Cloudflare ("cloudflared.exe service install <token>").
     */
    public static TokenInfo parseToken(String raw) {
        String token = cleanToken(raw);
        if (!TOKEN_CHARS.matcher(token).matches()) {
            throw new IllegalArgumentException("El token no tiene el formato de Cloudflare. Copie el token completo del túnel (empieza por \"eyJ\").");
        }
        try {
            String b64 = token.replace('-', '+').replace('_', '/');
            JsonNode json = MAPPER.readTree(new String(Base64.getMimeDecoder().decode(b64), StandardCharsets.UTF_8));
            String account = json.path("a").asText("");
            String tunnel = json.path("t").asText("");
            if (account.isEmpty() || tunnel.isEmpty() || json.path("s").asText("").isEmpty()) {
                throw new IllegalArgumentException("El token no contiene cuenta, túnel y secreto");
            }
            return new TokenInfo(account, tunnel);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("El token")) {
                throw e;
            }
            throw new IllegalArgumentException("El token no tiene el formato de Cloudflare. Copie el token completo del túnel (empieza por \"eyJ\").");
        } catch (IOException e) {
            throw new IllegalArgumentException("El token no tiene el formato de Cloudflare. Copie el token completo del túnel (empieza por \"eyJ\").");
        }
    }

    /** Si pegaron el comando entero, se queda con la última palabra; quita espacios y comillas. */
    static String cleanToken(String raw) {
        String text = raw == null ? "" : raw.trim().replace("\"", "").replace("'", "");
        String[] words = text.split("\\s+");
        return words[words.length - 1];
    }

    // ───────────── Estado ─────────────

    public boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().startsWith("windows");
    }

    /** null si se puede instalar desde aquí; si no, el motivo. */
    public String unavailableReason() {
        if (!isWindows()) {
            return "El túnel solo se puede instalar desde el panel en Windows.";
        }
        if (!Files.isRegularFile(mainWrapperExe())) {
            return "Solo funciona en el servicio instalado con el instalador (no se encontró " + mainWrapperExe() + ").";
        }
        return null;
    }

    public Map<String, Object> status() {
        Map<String, Object> map = new LinkedHashMap<>();
        String reason = unavailableReason();
        map.put("available", reason == null);
        map.put("unavailableReason", reason);
        map.put("serviceId", SERVICE_ID);
        map.put("cloudflaredVersion", CLOUDFLARED_VERSION);
        map.put("cloudflaredPresent", Files.isRegularFile(cloudflaredExe()));
        String state = isWindows() ? serviceState() : "NO_INSTALADO";
        map.put("state", state);
        map.put("installed", !"NO_INSTALADO".equals(state));
        String tunnelId = null;
        try {
            if (Files.isRegularFile(tokenFile())) {
                tunnelId = parseToken(Files.readString(tokenFile(), StandardCharsets.UTF_8)).tunnelId();
            }
        } catch (Exception e) {
            log.warn("TUNNEL_STATUS | no se pudo leer el token guardado: {}", e.getMessage());
        }
        map.put("tunnelId", tunnelId);
        return map;
    }

    /** RUNNING, STOPPED, START_PENDING, STOP_PENDING... o NO_INSTALADO. sc.exe traduce las etiquetas, no los estados. */
    String serviceState() {
        CommandResult r = run(List.of("sc.exe", "query", SERVICE_ID));
        if (r.exitCode() == 1060 || r.output().contains("1060")) {
            return "NO_INSTALADO";
        }
        for (String s : List.of("START_PENDING", "STOP_PENDING", "RUNNING", "STOPPED", "PAUSED")) {
            if (r.output().contains(s)) {
                return s;
            }
        }
        return "DESCONOCIDO";
    }

    // ───────────── Instalar / desinstalar ─────────────

    /** Instala (o reemplaza) el túnel con el token dado. Pensado para correr en AdminJobRunner. */
    public void install(String rawToken, AdminJobRunner.Job job) throws Exception {
        TokenInfo info = parseToken(rawToken);
        String reason = unavailableReason();
        if (reason != null) {
            throw new IllegalStateException(reason);
        }
        job.total = 4;
        Files.createDirectories(tunnelDir());

        job.message = "Descargando cloudflared " + CLOUDFLARED_VERSION;
        ensureCloudflared();
        job.step(true, null);

        job.message = "Quitando el túnel anterior";
        removeService();
        job.step(true, null);

        job.message = "Guardando la configuración";
        Files.createDirectories(tokenFile().getParent());
        Files.writeString(tokenFile(), cleanToken(rawToken), StandardCharsets.UTF_8);
        Files.copy(mainWrapperExe(), wrapperExe(), StandardCopyOption.REPLACE_EXISTING);
        Files.writeString(tunnelDir().resolve(SERVICE_ID + ".xml"), serviceXml(), StandardCharsets.UTF_8);
        job.step(true, null);

        job.message = "Registrando e iniciando el servicio " + SERVICE_ID;
        check(run(List.of(wrapperExe().toString(), "install")), "registrar el servicio");
        check(run(List.of(wrapperExe().toString(), "start")), "iniciar el servicio");
        job.step(true, null);

        log.info("TUNNEL_INSTALLED | tunnelId={} | service={}", info.tunnelId(), SERVICE_ID);
        job.result = status();
    }

    /** Detiene y borra el servicio y el token. Deja cloudflared descargado para una próxima instalación. */
    public Map<String, Object> uninstall() throws IOException {
        if (!isWindows()) {
            throw new IllegalStateException("El túnel solo se puede administrar desde el panel en Windows.");
        }
        removeService();
        Files.deleteIfExists(tokenFile());
        log.info("TUNNEL_UNINSTALLED | service={}", SERVICE_ID);
        return status();
    }

    private void removeService() {
        if ("NO_INSTALADO".equals(serviceState())) {
            return;
        }
        if (Files.isRegularFile(wrapperExe())) {
            run(List.of(wrapperExe().toString(), "stopwait"));
            check(run(List.of(wrapperExe().toString(), "uninstall")), "quitar el servicio anterior");
        } else {
            run(List.of("sc.exe", "stop", SERVICE_ID));
            check(run(List.of("sc.exe", "delete", SERVICE_ID)), "quitar el servicio anterior");
        }
    }

    private static void check(CommandResult r, String what) {
        if (r.exitCode() != 0) {
            throw new IllegalStateException("No se pudo " + what + " (código " + r.exitCode() + "): " + r.output().trim());
        }
    }

    /** Descarga cloudflared si falta o no coincide con la huella esperada; nunca deja un binario sin verificar. */
    void ensureCloudflared() throws Exception {
        Path exe = cloudflaredExe();
        if (Files.isRegularFile(exe) && CLOUDFLARED_SHA256.equalsIgnoreCase(sha256(exe))) {
            return;
        }
        Path part = tunnelDir().resolve("cloudflared.exe.part");
        download(URI.create(CLOUDFLARED_URL), part);
        String actual = sha256(part);
        if (!CLOUDFLARED_SHA256.equalsIgnoreCase(actual)) {
            Files.deleteIfExists(part);
            throw new IllegalStateException("La descarga de cloudflared no coincide con la huella esperada (SHA256 " + actual + "). No se instaló nada.");
        }
        Files.move(part, exe, StandardCopyOption.REPLACE_EXISTING);
    }

    void download(URI uri, Path target) throws Exception {
        HttpClient http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        HttpResponse<Path> response = http.send(
                HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(10)).GET().build(),
                HttpResponse.BodyHandlers.ofFile(target));
        if (response.statusCode() != 200) {
            Files.deleteIfExists(target);
            throw new IllegalStateException("No se pudo descargar cloudflared de GitHub (HTTP " + response.statusCode() + ")");
        }
    }

    static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[64 * 1024];
            int n;
            while ((n = in.read(buffer)) > 0) {
                digest.update(buffer, 0, n);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /** Configuración de WinSW para el túnel: cloudflared lee el token de un archivo, no de la línea de comandos. */
    String serviceXml() {
        return """
                <!-- Generado por el panel de RMPAY Service (TunnelService). Túnel de Cloudflare hacia los webhooks. -->
                <service>
                  <id>%s</id>
                  <name>RMPAY Tunnel (Cloudflare)</name>
                  <description>Túnel de Cloudflare para que Shopify llegue a los webhooks de RMPAY Service.</description>
                  <executable>%%BASE%%\\cloudflared.exe</executable>
                  <arguments>tunnel --no-autoupdate run --token-file "%s"</arguments>
                  <startmode>Automatic</startmode>
                  <onfailure action="restart" delay="10 sec"/>
                  <onfailure action="restart" delay="30 sec"/>
                  <onfailure action="restart" delay="60 sec"/>
                  <resetfailure>1 hour</resetfailure>
                  <logpath>%%BASE%%\\logs</logpath>
                  <log mode="roll-by-size">
                    <sizeThreshold>10240</sizeThreshold>
                    <keepFiles>5</keepFiles>
                  </log>
                </service>
                """.formatted(SERVICE_ID, tokenFile());
    }

    /** Ejecuta un comando sin consola ni shell (argumentos separados) y espera hasta 2 minutos. */
    CommandResult run(List<String> command) {
        try {
            Process p = new ProcessBuilder(new ArrayList<>(command)).redirectErrorStream(true).start();
            byte[] out = p.getInputStream().readAllBytes();
            if (!p.waitFor(2, TimeUnit.MINUTES)) {
                p.destroyForcibly();
                return new CommandResult(-1, "tiempo de espera agotado");
            }
            return new CommandResult(p.exitValue(), new String(out, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return new CommandResult(-1, e.getMessage());
        }
    }
}
