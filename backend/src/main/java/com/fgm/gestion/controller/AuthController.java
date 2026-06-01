package com.fgm.gestion.controller;

import com.fgm.gestion.auth.FgmJwtService;
import com.fgm.gestion.model.FgmAppUser;
import com.fgm.gestion.repository.FgmAppUserRepository;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentification — émet un JWT signé par ce backend (HS256).
 * L'annotation @CrossOrigin est un filet de sécurité en plus du CorsConfigurationSource.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
    RequestMethod.POST, RequestMethod.OPTIONS, RequestMethod.GET
})
public class AuthController {

    private final FgmAppUserRepository fgmAppUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final FgmJwtService fgmJwtService;

    @Value("${fgm.jwt.expiration-hours:12}")
    private long expirationHours;

    public AuthController(
            FgmAppUserRepository fgmAppUserRepository,
            PasswordEncoder passwordEncoder,
            FgmJwtService fgmJwtService) {
        this.fgmAppUserRepository = fgmAppUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.fgmJwtService = fgmJwtService;
    }

    /**
     * POST /api/auth/login
     * Body : { "username": "admin", "password": "admin" }
     * Retourne : { "access_token": "...", "token_type": "Bearer", "expires_in": 43200 }
     */
    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body != null ? body.getOrDefault("username", "").trim() : "";
        String password = body != null ? body.getOrDefault("password", "") : "";

        if (username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_request",
                    "message", "username et password sont requis"));
        }

        FgmAppUser user = fgmAppUserRepository.findByUsernameIgnoreCase(username)
                .or(() -> fgmAppUserRepository.findByEmailIgnoreCase(username))
                .orElse(null);

        if (user == null
                || user.getPasswordHash() == null
                || user.getPasswordHash().isBlank()
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_grant",
                    "message", "Identifiant ou mot de passe incorrect"));
        }

        try {
            String accessToken = fgmJwtService.createAccessToken(user);
            return ResponseEntity.ok(Map.of(
                    "access_token", accessToken,
                    "token_type",   "Bearer",
                    "expires_in",   expirationHours * 3600,
                    "username",     user.getUsername(),
                    "roles",        user.getRoles()
            ));
        } catch (JOSEException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "server_error", "message", e.getMessage()));
        }
    }

    /** Alias — /api/login redirige vers /api/auth/login pour compatibilité */
    @PostMapping("/api/login")
    public ResponseEntity<?> loginAlias(@RequestBody Map<String, String> body) {
        return login(body);
    }
}
