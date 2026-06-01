package com.fgm.gestion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;
import java.util.Arrays;
import java.util.List;

/**
 * Security — JWT maison. Rôles : ADMIN_FGM, SUPERVISEUR, INTERMEDIAIRE (+ alias ADMIN/USER).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    @Value("${fgm.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
    private String corsAllowedOrigins;

    public SecurityConfig(JwtDecoder jwtDecoder,
                          JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Preflight + endpoints publics
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/login").permitAll()
                // ADMIN / SUPERVISEUR / ADMIN_FGM
                .requestMatchers(HttpMethod.PUT,    "/api/parametrage/**").hasAnyRole("ADMIN", "ADMIN_FGM", "SUPERVISEUR")
                .requestMatchers(HttpMethod.POST,   "/api/tmm").hasAnyRole("ADMIN", "ADMIN_FGM", "SUPERVISEUR")
                .requestMatchers(HttpMethod.DELETE, "/api/tmm/**").hasAnyRole("ADMIN", "ADMIN_FGM", "SUPERVISEUR")
                .requestMatchers(HttpMethod.POST,   "/api/import/session").hasAnyRole("ADMIN", "ADMIN_FGM", "SUPERVISEUR", "USER")
                // USER + ADMIN (+ rôles FGM)
                .requestMatchers(HttpMethod.POST, "/api/global/run-all").hasAnyRole("ADMIN", "ADMIN_FGM", "SUPERVISEUR", "USER")
                .requestMatchers(HttpMethod.POST, "/api/seance/create").hasAnyRole("ADMIN", "ADMIN_FGM", "SUPERVISEUR", "USER")
                .requestMatchers(HttpMethod.POST, "/api/seances/preparer").hasAnyRole("ADMIN", "ADMIN_FGM", "SUPERVISEUR", "USER")
                .requestMatchers(HttpMethod.POST, "/api/positionnette/generate").hasAnyRole("ADMIN", "ADMIN_FGM", "SUPERVISEUR", "USER")
                .requestMatchers(HttpMethod.POST, "/api/risque/generate").hasAnyRole("ADMIN", "ADMIN_FGM", "SUPERVISEUR", "USER")
                .requestMatchers(HttpMethod.POST, "/api/mouvementbancaire/run").hasAnyRole("ADMIN", "ADMIN_FGM", "SUPERVISEUR", "USER")
                // Tout authentifié
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)))
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        cfg.setAllowedOrigins(origins);
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH","HEAD"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization","Content-Disposition","Content-Type"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
