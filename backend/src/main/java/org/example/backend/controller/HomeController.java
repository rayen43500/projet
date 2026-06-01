package com.fgm.gestion.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/api/user/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("authenticated", true);
        body.put("preferred_username", jwt.getClaimAsString("preferred_username"));
        body.put("email", jwt.getClaimAsString("email"));
        body.put("name", jwt.getClaimAsString("name"));
        body.put("realm_access", jwt.getClaim("realm_access"));
        body.put("resource_access", jwt.getClaim("resource_access"));
        Object ic = jwt.getClaim("intermediaire_code");
        if (ic == null) {
            ic = jwt.getClaim("code_intermediaire");
        }
        body.put("intermediaire_code", ic);
        return ResponseEntity.ok(body);
    }
}
