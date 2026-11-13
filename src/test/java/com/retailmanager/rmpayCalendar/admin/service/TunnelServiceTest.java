package com.retailmanager.rmpayCalendar.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Instalación del túnel de Cloudflare desde el panel (TunnelService) sin tocar Windows ni internet:
 * los comandos y la descarga se simulan.
 */
class TunnelServiceTest {

    private static final String TOKEN = Base64.getEncoder().encodeToString(
            "{\"a\":\"cuenta123\",\"t\":\"826bfd34-0000-4000-8000-000000000001\",\"s\":\"c2VjcmV0by1kZS1wcnVlYmE=\"}"
                    .getBytes(StandardCharsets.UTF_8));

    @TempDir
    Path base;

    /** Simula sc.exe / WinSW y la descarga; guarda los comandos ejecutados. */
    class FakeTunnel extends TunnelService {
        final List<List<String>> commands = new ArrayList<>();
        boolean installed;
        byte[] downloadContent = "no es cloudflared".getBytes(StandardCharsets.UTF_8);

        FakeTunnel() {
            super(base);
        }

        @Override
        CommandResult run(List<String> command) {
            commands.add(command);
            if (command.get(0).equals("sc.exe")) {
                return installed ? new CommandResult(0, "ESTADO : 4  RUNNING") : new CommandResult(1060, "ERROR 1060");
            }
            if (command.get(command.size() - 1).equals("install")) {
                installed = true;
            }
            return new CommandResult(0, "");
        }

        @Override
        void download(URI uri, Path target) throws Exception {
            Files.write(target, downloadContent);
        }
    }

    @Test
    void leeCuentaYTunelDelToken() {
        TunnelService.TokenInfo info = TunnelService.parseToken(TOKEN);
        assertEquals("cuenta123", info.accountTag());
        assertEquals("826bfd34-0000-4000-8000-000000000001", info.tunnelId());
    }

    @Test
    void aceptaElComandoCompletoQueMuestraCloudflare() {
        TunnelService.TokenInfo info = TunnelService.parseToken("  cloudflared.exe service install " + TOKEN + "\n");
        assertEquals("826bfd34-0000-4000-8000-000000000001", info.tunnelId());
    }

    @Test
    void rechazaTokensInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> TunnelService.parseToken(""));
        assertThrows(IllegalArgumentException.class, () -> TunnelService.parseToken("hola mundo"));
        String sinSecreto = Base64.getEncoder().encodeToString("{\"a\":\"cuenta123\",\"t\":\"tunel-sin-secreto-xxxxxxxx\"}".getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalArgumentException.class, () -> TunnelService.parseToken(sinSecreto));
        assertThrows(IllegalArgumentException.class, () -> TunnelService.parseToken(TOKEN + "; del C:\\"));
    }

    @Test
    void noDejaUnaDescargaConHuellaDistinta() throws Exception {
        FakeTunnel tunnel = new FakeTunnel();
        Files.createDirectories(tunnel.tunnelDir());

        IllegalStateException e = assertThrows(IllegalStateException.class, tunnel::ensureCloudflared);
        assertTrue(e.getMessage().contains("huella"));
        assertFalse(Files.exists(tunnel.cloudflaredExe()));
        assertFalse(Files.exists(tunnel.tunnelDir().resolve("cloudflared.exe.part")));
    }

    @Test
    void sinElServicioInstaladoNoSePuedeInstalar() {
        FakeTunnel tunnel = new FakeTunnel();
        assumeTrue(tunnel.isWindows());
        assertTrue(tunnel.unavailableReason().contains("RMPAYService.exe"));
        AdminJobRunner.Job job = new AdminJobRunner.Job("Instalar túnel", "test");
        assertThrows(IllegalStateException.class, () -> tunnel.install(TOKEN, job));
    }

    @Test
    void instalaElServicioConElTokenEnArchivo() throws Exception {
        FakeTunnel tunnel = new FakeTunnel();
        assumeTrue(tunnel.isWindows());
        Files.writeString(tunnel.mainWrapperExe(), "winsw");
        // cloudflared ya descargado y "verificado": se simula con una huella calculada del contenido
        Files.createDirectories(tunnel.tunnelDir());
        TunnelService verified = new FakeTunnel() {
            @Override
            void ensureCloudflared() {
            }
        };
        AdminJobRunner.Job job = new AdminJobRunner.Job("Instalar túnel", "test");

        verified.install("cloudflared.exe service install " + TOKEN, job);

        assertEquals(TOKEN, Files.readString(verified.tokenFile()));
        assertTrue(verified.tokenFile().startsWith(base.resolve("config")), "el token va en config\\ (solo administradores)");
        String xml = Files.readString(verified.tunnelDir().resolve("RMPAYTunnel.xml"));
        assertTrue(xml.contains("--token-file"));
        assertFalse(xml.contains(TOKEN), "el token no va en la línea de comandos del servicio");
        assertTrue(xml.contains("<executable>%BASE%\\cloudflared.exe</executable>"));
        assertTrue(Files.exists(verified.wrapperExe()));
        assertEquals(4, job.done);
        assertEquals("RUNNING", job.result.get("state"));
        assertEquals("826bfd34-0000-4000-8000-000000000001", job.result.get("tunnelId"));
    }

    @Test
    void desinstalarQuitaServicioYToken() throws Exception {
        FakeTunnel tunnel = new FakeTunnel();
        assumeTrue(tunnel.isWindows());
        tunnel.installed = true;
        Files.createDirectories(tunnel.tunnelDir());
        Files.writeString(tunnel.wrapperExe(), "winsw");
        Files.createDirectories(tunnel.tokenFile().getParent());
        Files.writeString(tunnel.tokenFile(), TOKEN);

        tunnel.uninstall();

        assertFalse(Files.exists(tunnel.tokenFile()));
        assertTrue(tunnel.commands.stream().anyMatch(c -> c.get(c.size() - 1).equals("uninstall")));
    }
}
