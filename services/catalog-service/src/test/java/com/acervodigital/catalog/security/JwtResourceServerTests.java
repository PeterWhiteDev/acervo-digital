package com.acervodigital.catalog.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtResourceServerTests {

    private static final String TEST_SECRET = "local-test-only-secret-key-for-hmac-sha256-32";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void acceptsJwtSignedWithIdentityServiceHs256Secret() throws Exception {
        UUID collectorId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        Instant issuedAt = Instant.now();
        SecretKey secretKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .issuer("acervo-identity")
                .subject(collectorId.toString())
                .claim("iat", issuedAt.getEpochSecond())
                .claim("exp", issuedAt.plusSeconds(900).getEpochSecond())
                .claim("roles", List.of("USER"))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        mockMvc.perform(get("/api/v1/item-types")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").isArray());
    }
}