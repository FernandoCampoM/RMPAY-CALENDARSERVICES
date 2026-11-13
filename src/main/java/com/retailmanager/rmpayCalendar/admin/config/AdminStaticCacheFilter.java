package com.retailmanager.rmpayCalendar.admin.config;

import java.io.IOException;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Archivos del panel (index.html, admin.js, admin.css, guías): "Cache-Control: no-cache" para que
 * el navegador los revalide en cada carga. Sin esto, tras instalar una versión nueva el navegador
 * puede mezclar un index.html nuevo con un admin.js viejo (las vistas nuevas abren el Panel).
 * Revalidar es barato: si no cambiaron, el servidor responde 304 por Last-Modified.
 * No toca /admin/api (lo manejan AdminSecurityFilters y los controladores).
 */
@Configuration
public class AdminStaticCacheFilter {

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> adminStaticNoCacheFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                if (!request.getRequestURI().contains("/admin/api/")) {
                    response.setHeader("Cache-Control", "no-cache");
                }
                chain.doFilter(request, response);
            }
        });
        registration.addUrlPatterns("/admin/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        return registration;
    }
}
