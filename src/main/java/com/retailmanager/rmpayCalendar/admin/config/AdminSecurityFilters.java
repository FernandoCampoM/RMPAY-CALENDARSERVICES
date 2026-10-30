package com.retailmanager.rmpayCalendar.admin.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Filtros del panel admin. No se usa Spring Security porque al agregarlo bloquearía
 * por defecto todos los endpoints existentes.
 *  1. Puerto: /admin/** solo responde en admin.port y el resto de la app no responde en admin.port.
 *  2. Sesión: /admin/api/** exige sesión iniciada (salvo el login) y el header X-Admin-Request
 *     en métodos que modifican, para impedir CSRF desde otros sitios.
 */
@Configuration
public class AdminSecurityFilters {

    public static final String SESSION_USER = "ADMIN_USER";
    public static final String SESSION_MUST_CHANGE = "ADMIN_MUST_CHANGE_PASSWORD";
    public static final String CSRF_HEADER = "X-Admin-Request";

    @Value("${admin.port:47821}")
    private int adminPort;

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> adminPortFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                String path = pathOf(request);
                boolean adminPath = path.equals("/admin") || path.startsWith("/admin/");
                boolean onAdminPort = request.getLocalPort() == adminPort;

                if (onAdminPort && path.equals("/")) {
                    response.sendRedirect("/admin/index.html");
                    return;
                }
                if (adminPath != onAdminPort) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                chain.doFilter(request, response);
            }
        });
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> adminAuthFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                String path = pathOf(request);
                String method = request.getMethod();

                if (!"GET".equals(method) && !"HEAD".equals(method) && !"OPTIONS".equals(method)
                        && request.getHeader(CSRF_HEADER) == null) {
                    writeJson(response, HttpServletResponse.SC_FORBIDDEN, "Solicitud no permitida");
                    return;
                }
                if (path.equals("/admin/api/auth/login")) {
                    chain.doFilter(request, response);
                    return;
                }
                HttpSession session = request.getSession(false);
                if (session == null || session.getAttribute(SESSION_USER) == null) {
                    writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Sesión no iniciada");
                    return;
                }
                if (session.getAttribute(SESSION_MUST_CHANGE) != null && !path.startsWith("/admin/api/auth/")) {
                    writeJson(response, HttpServletResponse.SC_FORBIDDEN, "Debe cambiar la contraseña antes de continuar");
                    return;
                }
                chain.doFilter(request, response);
            }
        });
        registration.addUrlPatterns("/admin/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }

    private static String pathOf(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return (context != null && !context.isEmpty() && uri.startsWith(context)) ? uri.substring(context.length()) : uri;
    }

    private static void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"error\":\"" + message + "\"}");
    }
}
