package com.fgm.gestion.auth;

import com.fgm.gestion.model.FgmAppUser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class FgmJwtService {

    @Value("${fgm.jwt.secret}")
    private String secret;

    @Value("${fgm.jwt.issuer:http://localhost:8081}")
    private String issuer;

    @Value("${fgm.jwt.audience:fgm-frontend}")
    private String audience;

    @Value("${fgm.jwt.expiration-hours:12}")
    private long expirationHours;

    public String createAccessToken(FgmAppUser user) throws JOSEException {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("fgm.jwt.secret must be at least 32 bytes (256 bits) for HS256");
        }
        MACSigner signer = new MACSigner(secretBytes);
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationHours * 3600);

        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", user.getRoles());

        JWTClaimsSet.Builder b = new JWTClaimsSet.Builder()
                .subject(user.getUsername() != null ? user.getUsername() : user.getEmail())
                .issuer(issuer)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .audience(audience)
                .claim("realm_access", realmAccess)
                .claim("preferred_username", user.getUsername() != null ? user.getUsername() : user.getEmail())
                .claim("email", user.getEmail())
                .claim("name", user.getFullName() != null ? user.getFullName() : user.getUsername());

        if (user.getCodeIntermediaire() != null) {
            b.claim("intermediaire_code", user.getCodeIntermediaire());
        }

        JWTClaimsSet claims = b.build();
        SignedJWT signed = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        signed.sign(signer);
        return signed.serialize();
    }
}
