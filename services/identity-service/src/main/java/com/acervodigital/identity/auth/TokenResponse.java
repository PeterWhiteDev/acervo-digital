package com.acervodigital.identity.auth;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}