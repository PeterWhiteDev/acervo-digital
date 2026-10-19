package com.acervodigital.identity.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class TestIdentityJwt {

    private static final String TEST_SECRET = "local-test-only-secret-key-for-hmac-sha256-32";

    private TestIdentityJwt() {
    }

    public static RequestPostProcessor asUser(UUID collectorId) {
        return withRole(collectorId, "USER");
    }

    public static RequestPostProcessor asAdmin(UUID collectorId) {
        return withRole(collectorId, "ADMIN");
    }

    private static RequestPostProcessor withRole(UUID collectorId, String role) {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .issuer("acervo-identity")
                .subject(collectorId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .claim("roles", List.of(role))
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }
}