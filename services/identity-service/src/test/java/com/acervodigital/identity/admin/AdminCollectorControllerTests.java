package com.acervodigital.identity.admin;

import com.acervodigital.identity.account.CollectorAccount;
import com.acervodigital.identity.account.CollectorAccountRepository;
import com.acervodigital.identity.account.CollectorRole;
import com.acervodigital.identity.auth.CreateTokenRequest;
import com.acervodigital.identity.security.TestIdentityJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminCollectorControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CollectorAccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

        @BeforeEach
        void clearAccounts() {
                accountRepository.deleteAllInBatch();
        }

    @Test
    void protectsAdministrationAndListsUsersWithoutPasswordHashes() throws Exception {
        CollectorAccount admin = createAccount("admin", CollectorRole.ADMIN);
        CollectorAccount user = createAccount("user", CollectorRole.USER);

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/users").with(TestIdentityJwt.asUser(user.getId())))
                .andExpect(status().isForbidden());

        String response = mockMvc.perform(get("/api/v1/admin/users").with(TestIdentityJwt.asAdmin(admin.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.collectorId == '%s')].displayName".formatted(user.getId()))
                        .value(org.hamcrest.Matchers.contains("user Coleccionista")))
                .andExpect(jsonPath("$[?(@.collectorId == '%s')].role".formatted(user.getId()))
                        .value(org.hamcrest.Matchers.contains("USER")))
                .andExpect(jsonPath("$[?(@.collectorId == '%s')].active".formatted(user.getId()))
                        .value(org.hamcrest.Matchers.contains(true)))
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain("passwordHash", "password_hash");
    }

    @Test
    void adminCanCreateUpdateResetAndDeactivateOtherAccounts() throws Exception {
        CollectorAccount admin = createAccount("manage", CollectorRole.ADMIN);
        String createRequest = objectMapper.writeValueAsString(new CreateAdminCollectorRequest(
                "New.User@example.test", "initial-password-123", "  Nuevo usuario  "));

        String createdResponse = mockMvc.perform(post("/api/v1/admin/users")
                        .with(TestIdentityJwt.asAdmin(admin.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new.user@example.test"))
                .andExpect(jsonPath("$.displayName").value("Nuevo usuario"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();
        AdminCollectorResponse created = objectMapper.readValue(createdResponse, AdminCollectorResponse.class);

        mockMvc.perform(put("/api/v1/admin/users/{id}", created.collectorId())
                        .with(TestIdentityJwt.asAdmin(admin.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateAdminCollectorRequest(
                                created.email(), created.displayName(), CollectorRole.ADMIN, true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(put("/api/v1/admin/users/{id}/password", created.collectorId())
                        .with(TestIdentityJwt.asAdmin(admin.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetAdminCollectorPasswordRequest("replacement-password-456"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTokenRequest(
                                created.email(), "replacement-password-456"))))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/admin/users/{id}", created.collectorId())
                        .with(TestIdentityJwt.asAdmin(admin.getId())))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTokenRequest(
                                created.email(), "replacement-password-456"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCannotManageSelfOrRemoveLastActiveAdministrator() throws Exception {
        CollectorAccount admin = createAccount("only-admin", CollectorRole.ADMIN);
        UpdateAdminCollectorRequest demotion = new UpdateAdminCollectorRequest(
                admin.getEmail(), admin.getProfile().getDisplayName(), CollectorRole.USER, true);
        String updateBody = objectMapper.writeValueAsString(demotion);
        var token = TestIdentityJwt.asAdmin(admin.getId());

        mockMvc.perform(put("/api/v1/admin/users/{id}", admin.getId())
                        .with(token).contentType(MediaType.APPLICATION_JSON).content(updateBody))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/admin/users/{id}/password", admin.getId())
                        .with(TestIdentityJwt.asAdmin(admin.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetAdminCollectorPasswordRequest("new-password-789"))))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/admin/users/{id}", admin.getId())
                        .with(TestIdentityJwt.asAdmin(admin.getId())))
                .andExpect(status().isForbidden());

        CollectorAccount otherAdmin = createAccount("other-admin", CollectorRole.ADMIN);
        mockMvc.perform(put("/api/v1/admin/users/{id}", admin.getId())
                        .with(TestIdentityJwt.asAdmin(otherAdmin.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/admin/users/{id}", otherAdmin.getId())
                        .with(TestIdentityJwt.asAdmin(admin.getId())))
                .andExpect(status().isConflict());
    }

    private CollectorAccount createAccount(String prefix, CollectorRole role) {
        String email = prefix + "-" + UUID.randomUUID() + "@example.test";
        CollectorAccount account = CollectorAccount.register(
                email,
                passwordEncoder.encode("practice-password-123"),
                prefix + " Coleccionista"
        );
        CollectorAccount saved = accountRepository.saveAndFlush(account);
        if (role == CollectorRole.ADMIN) {
            saved.update(saved.getEmail(), saved.getProfile().getDisplayName(), CollectorRole.ADMIN, true);
                        saved = accountRepository.saveAndFlush(saved);
        }
        return saved;
    }
}