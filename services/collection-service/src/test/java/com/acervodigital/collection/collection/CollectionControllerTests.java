package com.acervodigital.collection.collection;

import com.acervodigital.collection.security.TestJwt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CollectionControllerTests {

    private static final UUID OWNER_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID ADMIN_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void requiresAuthenticationForPrivateCollectionRoutes() throws Exception {
        mockMvc.perform(get("/api/v1/collections"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/public/collections"))
                .andExpect(status().isOk());

        assertEquals(HttpStatus.UNAUTHORIZED,
                restTemplate.getForEntity("/api/v1/collections", String.class).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED,
                restTemplate.getForEntity("/error", String.class).getStatusCode());
    }

    @Test
    void createsAndListsOnlyOwnedCollections() throws Exception {
        String name = "Colección privada " + UUID.randomUUID();
        MvcResult created = createCollection(OWNER_ID, name, "PRIVATE");
        String collectionId = collectionId(created);

        mockMvc.perform(get("/api/v1/collections").with(TestJwt.asUser(OWNER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.hasItem(collectionId)));
        mockMvc.perform(get("/api/v1/collections").with(TestJwt.asUser(OTHER_OWNER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(0)));
        mockMvc.perform(get("/api/v1/collections/{id}", collectionId).with(TestJwt.asUser(OTHER_OWNER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicCollectionsCanBeReadWithoutAuthentication() throws Exception {
        MvcResult created = createCollection(OWNER_ID, "Colección pública", "PUBLIC");
        String collectionId = collectionId(created);

        mockMvc.perform(get("/api/v1/public/collections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(org.hamcrest.Matchers.hasItem(collectionId)));
        mockMvc.perform(get("/api/v1/public/collections/{id}", collectionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));

        MvcResult privateCollection = createCollection(OWNER_ID, "Colección no pública", "PRIVATE");
        mockMvc.perform(get("/api/v1/public/collections/{id}", collectionId(privateCollection)))
                .andExpect(status().isNotFound());

        assertEquals(HttpStatus.OK,
                restTemplate.getForEntity("/api/v1/public/collections", String.class).getStatusCode());
        assertEquals(HttpStatus.OK,
                restTemplate.getForEntity("/api/v1/public/collections/{id}", String.class,
                        collectionId).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                restTemplate.getForEntity("/api/v1/public/collections/{id}", String.class,
                        collectionId(privateCollection)).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                restTemplate.getForEntity("/api/v1/public/collections/{id}", String.class,
                        UUID.randomUUID()).getStatusCode());
    }

    @Test
    void ownerCanUpdateAndDeleteButAdminCanManageAnotherOwnersCollection() throws Exception {
        MvcResult created = createCollection(OWNER_ID, "Colección editable", "PRIVATE");
        String collectionId = collectionId(created);

        mockMvc.perform(put("/api/v1/collections/{id}", collectionId)
                        .with(TestJwt.asUser(OTHER_OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest("No autorizado", "PRIVATE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/collections/{id}", collectionId)
                        .with(TestJwt.asUser(OWNER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest("Colección actualizada", "PUBLIC")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Colección actualizada"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));

        mockMvc.perform(delete("/api/v1/collections/{id}", collectionId)
                        .with(TestJwt.asAdmin(ADMIN_ID)))
                .andExpect(status().isNoContent());
    }

    private MvcResult createCollection(UUID ownerId, String name, String visibility) throws Exception {
        return mockMvc.perform(post("/api/v1/collections")
                        .with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Descripción de prueba",
                                  "visibility": "%s"
                                }
                                """.formatted(name, visibility)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn();
    }

    private String collectionId(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asText();
    }

    private String updateRequest(String name, String visibility) {
        return """
                {
                  "name": "%s",
                  "description": "Descripción modificada",
                  "visibility": "%s"
                }
                """.formatted(name, visibility);
    }
}