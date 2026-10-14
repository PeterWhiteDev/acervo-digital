package com.acervodigital.identity.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTests {

    private static final String TEST_SECRET = "local-test-only-secret-key-for-hmac-sha256-32";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registersUserAndIssuesSignedJwt() throws Exception {
        String email = "collector-" + UUID.randomUUID() + "@example.test";
        String password = "practice-password-123";
        String request = """
                {
                  "email": "%s",
                  "password": "%s",
                  "displayName": "Coleccionista",
                  "role": "ADMIN"
                }
                """.formatted(email, password);

        MvcResult registration = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value("Coleccionista"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        JsonNode collector = objectMapper.readTree(registration.getResponse().getContentAsString());
        UUID collectorId = UUID.fromString(collector.get("collectorId").asText());
        String tokenRequest = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        MvcResult tokenResult = mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900))
                .andReturn();

        String token = objectMapper.readTree(tokenResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .requireIssuer("acervo-identity")
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(collectorId.toString(), claims.getSubject());
        assertEquals(List.of("USER"), claims.get("roles"));
        assertNotNull(claims.getExpiration());
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        String email = "duplicate-" + UUID.randomUUID() + "@example.test";
        String request = """
                {
                  "email": "%s",
                  "password": "practice-password-123",
                  "displayName": "Coleccionista"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "missing@example.test",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}