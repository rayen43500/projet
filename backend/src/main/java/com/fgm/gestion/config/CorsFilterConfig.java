package com.fgm.gestion.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Filtre CORS priorité HIGHEST_PRECEDENCE — s'exécute AVANT Spring Security.
 * Les requêtes OPTIONS (preflight) reçoivent les headers CORS et un 200
 * immédiat, sans passer par l'authentification.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilterConfig implements Filter {

    private final Set<String> allowedOrigins;

    public CorsFilterConfig(
            @Value("${fgm.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
            String originsConfig) {
        this.allowedOrigins = new HashSet<>(
            Arrays.stream(originsConfig.split(","))
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .toList()
        );
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        String origin = request.getHeader("Origin");

        String allowOrigin = (origin != null && allowedOrigins.contains(origin))
                ? origin
                : (allowedOrigins.isEmpty() ? "*" : allowedOrigins.iterator().next());

        response.setHeader("Access-Control-Allow-Origin",      allowOrigin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods",     "GET,POST,PUT,DELETE,OPTIONS,PATCH,HEAD");
        response.setHeader("Access-Control-Allow-Headers",     "Authorization,Content-Type,Accept,Origin,X-Requested-With");
        response.setHeader("Access-Control-Expose-Headers",    "Authorization,Content-Disposition");
        response.setHeader("Access-Control-Max-Age",           "3600");

        // Preflight — répondre 200 immédiatement, ne pas continuer la chaîne
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }
}
