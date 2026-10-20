package com.acervodigital.collection.admin;

import com.acervodigital.collection.catalog.CatalogClient;
import com.acervodigital.collection.catalog.CatalogItemSummary;
import com.acervodigital.collection.collection.CollectionVisibility;
import com.acervodigital.collection.collection.CreateCollectionRequest;
import com.acervodigital.collection.copy.CopyCondition;
import com.acervodigital.collection.security.TestJwt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminCollectionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogClient catalogClient;

    @Test
    void adminCanSeeOnlyCollectionItemAndLocationCountsForAnotherCollector() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID catalogItemId = UUID.randomUUID();
        when(catalogClient.findItem(catalogItemId)).thenReturn(new CatalogItemSummary(catalogItemId, "Ficha"));
        String locationId = createLocation(ownerId, "Estantería azul");
        String firstCollectionId = createCollection(ownerId, "Colección privada", "PRIVATE");
        String secondCollectionId = createCollection(ownerId, "Colección pública", "PUBLIC");
        createCopy(ownerId, firstCollectionId, catalogItemId, locationId);
        createCopy(ownerId, firstCollectionId, catalogItemId, locationId);
        createCopy(ownerId, firstCollectionId, catalogItemId, null);
        createCopy(ownerId, secondCollectionId, catalogItemId, locationId);

        MvcResult result = mockMvc.perform(get("/api/v1/admin/collectors/{collectorId}/collections", ownerId)
                        .with(TestJwt.asAdmin(adminId)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(2, response.get("collectionCount").asInt());

        JsonNode privateCollection = findCollection(response, firstCollectionId);
        assertEquals("Colección privada", privateCollection.get("name").asText());
        assertEquals(3, privateCollection.get("itemCount").asInt());
        assertEquals(2, privateCollection.get("itemsByLocation").get(0).get("itemCount").asInt());
        assertEquals("Estantería azul", privateCollection.get("itemsByLocation").get(0).get("locationName").asText());
        assertEquals(1, privateCollection.get("unassignedItemCount").asInt());

        JsonNode publicCollection = findCollection(response, secondCollectionId);
        assertEquals(1, publicCollection.get("itemCount").asInt());
        assertEquals(1, publicCollection.get("itemsByLocation").get(0).get("itemCount").asInt());
        assertEquals(0, publicCollection.get("unassignedItemCount").asInt());
        org.junit.jupiter.api.Assertions.assertFalse(result.getResponse().getContentAsString().contains("Ficha"));
    }

    @Test
    void regularCollectorCannotReadAnotherCollectorsCollectionSummary() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/admin/collectors/{collectorId}/collections", ownerId)
                        .with(TestJwt.asUser(userId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSummaryRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/collectors/{collectorId}/collections", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private String createCollection(UUID ownerId, String name, String visibility) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/collections").with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCollectionRequest(
                                name, null, CollectionVisibility.valueOf(visibility)))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String createLocation(UUID ownerId, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/locations").with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("name", name))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private void createCopy(UUID ownerId, String collectionId, UUID catalogItemId, String locationId) throws Exception {
        String request = objectMapper.writeValueAsString(new CopyRequest(
                catalogItemId,
                CopyCondition.GOOD,
                locationId == null ? null : UUID.fromString(locationId),
                LocalDate.of(2025, 1, 1)
        ));
        mockMvc.perform(post("/api/v1/collections/{collectionId}/copies", collectionId)
                        .with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
    }

    private JsonNode findCollection(JsonNode response, String collectionId) {
        for (JsonNode collection : response.get("collections")) {
            if (collection.get("collectionId").asText().equals(collectionId)) {
                return collection;
            }
        }
        throw new AssertionError("Collection summary not found: " + collectionId);
    }

    private record CopyRequest(UUID catalogItemId, CopyCondition condition, UUID locationId, LocalDate acquiredOn) {
    }
}