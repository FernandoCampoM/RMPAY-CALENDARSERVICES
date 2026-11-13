package com.retailmanager.rmpayCalendar.admin.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.slf4j.Slf4j;

/**
 * Abre un segundo puerto (admin.port) para el panel admin, con el mismo certificado SSL
 * que el puerto principal. AdminPortFilter se encarga de separar qué responde en cada puerto.
 * Depende de: server.ssl.* y admin.port en application.properties.
 */
@Slf4j
@Configuration
public class AdminPortConfig {

    @Value("${admin.port:47821}")
    private int adminPort;
    @Value("${server.ssl.key-store:}")
    private String keyStore;
    @Value("${server.ssl.key-store-password:}")
    private String keyStorePassword;
    @Value("${server.ssl.key-store-type:PKCS12}")
    private String keyStoreType;
    @Value("${server.ssl.key-alias:}")
    private String keyAlias;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> adminConnectorCustomizer(ResourceLoader resourceLoader) {
        return factory -> factory.addAdditionalTomcatConnectors(createConnector(resourceLoader));
    }

    @Bean
    public WebMvcConfigurer adminViewControllers() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addRedirectViewController("/admin", "/admin/index.html");
                registry.addRedirectViewController("/admin/", "/admin/index.html");
            }
        };
    }

    private Connector createConnector(ResourceLoader resourceLoader) {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(adminPort);

        if (keyStore == null || keyStore.isBlank()) {
            log.warn("ADMIN_PORT | port={} | ssl=false (no hay server.ssl.key-store)", adminPort);
            return connector;
        }

        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        connector.setScheme("https");
        connector.setSecure(true);
        protocol.setSSLEnabled(true);

        SSLHostConfig sslHostConfig = new SSLHostConfig();
        SSLHostConfigCertificate certificate =
                new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.UNDEFINED);
        certificate.setCertificateKeystoreFile(resolveKeyStore(resourceLoader));
        certificate.setCertificateKeystorePassword(keyStorePassword);
        certificate.setCertificateKeystoreType(keyStoreType);
        if (keyAlias != null && !keyAlias.isBlank()) {
            certificate.setCertificateKeyAlias(keyAlias);
        }
        sslHostConfig.addCertificate(certificate);
        protocol.addSslHostConfig(sslHostConfig);

        log.info("ADMIN_PORT | port={} | ssl=true", adminPort);
        return connector;
    }

    /**
     * Tomcat necesita una ruta de archivo; si el keystore está en el classpath (dentro del jar)
     * se copia a un archivo temporal.
     */
    private String resolveKeyStore(ResourceLoader resourceLoader) {
        try {
            Resource resource = resourceLoader.getResource(keyStore);
            if (resource.isFile()) {
                return resource.getFile().getAbsolutePath();
            }
            File temp = File.createTempFile("admin-keystore", ".p12");
            temp.deleteOnExit();
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return temp.getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el keystore para el puerto admin: " + keyStore, e);
        }
    }
}
