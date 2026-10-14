package com.acervodigital.identity.security;

import com.acervodigital.identity.account.CollectorAccount;
import com.acervodigital.identity.auth.TokenResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtTokenService {

    private final SecretKey signingKey;
    private final String issuer;
    private final long accessTokenMinutes;

    public JwtTokenService(
            @Value("${acervo.security.jwt.secret}") String secret,
            @Value("${acervo.security.jwt.issuer}") String issuer,
            @Value("${acervo.security.jwt.access-token-minutes}") long accessTokenMinutes
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public TokenResponse issue(CollectorAccount account) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(accessTokenMinutes, ChronoUnit.MINUTES);
        String token = Jwts.builder()
                .issuer(issuer)
                .subject(account.getId().toString())
            .claim("iat", issuedAt.getEpochSecond())
            .claim("exp", expiresAt.getEpochSecond())
                .claim("roles", List.of(account.getRole().name()))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
        return new TokenResponse(token, "Bearer", ChronoUnit.SECONDS.between(issuedAt, expiresAt));
    }
}