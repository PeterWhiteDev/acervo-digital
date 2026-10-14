package com.acervodigital.catalog.itemtype;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtCurrentCollectorProvider implements CurrentCollectorProvider {

    @Override
    public UUID getCurrentCollectorId() {
        return UUID.fromString(currentJwt().getToken().getSubject());
    }

    @Override
    public boolean isAdmin() {
        return currentJwt().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private JwtAuthenticationToken currentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication;
        }
        throw new IllegalStateException("Se requiere una autenticación JWT válida");
    }
}