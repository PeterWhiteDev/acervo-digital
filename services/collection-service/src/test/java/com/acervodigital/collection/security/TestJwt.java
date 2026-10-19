package com.acervodigital.collection.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public final class TestJwt {

    private TestJwt() {
    }

    public static RequestPostProcessor asUser(UUID collectorId) {
        return asRole(collectorId, "USER");
    }

    public static RequestPostProcessor asAdmin(UUID collectorId) {
        return asRole(collectorId, "ADMIN");
    }

    private static RequestPostProcessor asRole(UUID collectorId, String role) {
        return jwt()
                .jwt(token -> token.subject(collectorId.toString()).claim("roles", List.of(role)))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }
}