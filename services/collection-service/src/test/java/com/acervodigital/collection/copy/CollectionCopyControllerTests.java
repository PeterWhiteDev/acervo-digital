package com.acervodigital.collection.copy;

import com.acervodigital.collection.catalog.CatalogClient;
import com.acervodigital.collection.catalog.CatalogItemSummary;
import com.acervodigital.collection.collection.CreateCollectionRequest;
import com.acervodigital.collection.collection.CollectionVisibility;
import com.acervodigital.collection.security.TestJwt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CollectionCopyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogClient catalogClient;

    @Test
    void ownerCanCreateReadUpdateAndDeleteCopiesWithOptionalLocation() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID catalogItemId = UUID.randomUUID();
        when(catalogClient.findItem(catalogItemId)).thenReturn(new CatalogItemSummary(catalogItemId, "Libro de prueba"));
        String collectionId = createCollection(ownerId, "PRIVATE");
        String locationId = createLocation(ownerId, "Estanteria");

        String copyId = createCopy(ownerId, collectionId, copyRequest(catalogItemId, locationId,
                CopyCondition.GOOD, "2024-01-15", "12.50", "15.00", "  Primera edicion  "));
        mockMvc.perform(get("/api/v1/collections/{collectionId}/copies", collectionId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(copyId)))
                .andExpect(jsonPath("$[0].catalogItemTitle").value("Libro de prueba"))
                .andExpect(jsonPath("$[0].condition").value("GOOD"))
                .andExpect(jsonPath("$[0].locationId").value(locationId))
                .andExpect(jsonPath("$[0].purchasePrice").value(12.5))
                .andExpect(jsonPath("$[0].currentValue").value(15.0))
                .andExpect(jsonPath("$[0].notes").value("Primera edicion"));

        mockMvc.perform(get("/api/v1/collections/{collectionId}/copies/{copyId}", collectionId, copyId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acquiredOn").value("2024-01-15"));

        UUID replacementItemId = UUID.randomUUID();
        when(catalogClient.findItem(replacementItemId))
                .thenReturn(new CatalogItemSummary(replacementItemId, "Otro libro"));
        mockMvc.perform(put("/api/v1/collections/{collectionId}/copies/{copyId}", collectionId, copyId)
                        .with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(copyRequest(replacementItemId, null, CopyCondition.LIKE_NEW,
                                "2025-02-01", null, "18.00", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.catalogItemTitle").value("Otro libro"))
                .andExpect(jsonPath("$.condition").value("LIKE_NEW"))
                .andExpect(jsonPath("$.locationId").doesNotExist())
                .andExpect(jsonPath("$.purchasePrice").doesNotExist())
                .andExpect(jsonPath("$.notes").doesNotExist());

        mockMvc.perform(delete("/api/v1/collections/{collectionId}/copies/{copyId}", collectionId, copyId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/collections/{collectionId}/copies/{copyId}", collectionId, copyId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void permitsMultipleCopiesOfTheSameCatalogItem() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID catalogItemId = UUID.randomUUID();
        when(catalogClient.findItem(catalogItemId)).thenReturn(new CatalogItemSummary(catalogItemId, "Juego"));
        String collectionId = createCollection(ownerId, "PRIVATE");

        String firstCopyId = createCopy(ownerId, collectionId, copyRequest(catalogItemId, null,
                CopyCondition.NEW, "2024-01-01", null, null, null));
        String secondCopyId = createCopy(ownerId, collectionId, copyRequest(catalogItemId, null,
                CopyCondition.POOR, "2024-02-01", null, null, null));

        mockMvc.perform(get("/api/v1/collections/{collectionId}/copies", collectionId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(firstCopyId)))
                .andExpect(jsonPath("$[*].id").value(hasItem(secondCopyId)))
                .andExpect(jsonPath("$[*].catalogItemId").value(org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is(catalogItemId.toString()))));
    }

    @Test
    void isolatesCopiesAndAllowsAdminManagementWithoutChangingOwner() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherOwnerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID catalogItemId = UUID.randomUUID();
        when(catalogClient.findItem(catalogItemId)).thenReturn(new CatalogItemSummary(catalogItemId, "Pelicula"));
        String collectionId = createCollection(ownerId, "PRIVATE");
        String otherCollectionId = createCollection(otherOwnerId, "PRIVATE");
        String copyId = createCopy(ownerId, collectionId, copyRequest(catalogItemId, null,
                CopyCondition.ACCEPTABLE, "2023-03-01", null, null, null));

        mockMvc.perform(get("/api/v1/collections/{collectionId}/copies", collectionId)
                        .with(TestJwt.asUser(otherOwnerId)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/collections/{collectionId}/copies", otherCollectionId)
                        .with(TestJwt.asUser(otherOwnerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(not(hasItem(copyId))));
        mockMvc.perform(put("/api/v1/collections/{collectionId}/copies/{copyId}", collectionId, copyId)
                        .with(TestJwt.asUser(otherOwnerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(copyRequest(catalogItemId, null, CopyCondition.POOR,
                                "2023-03-01", null, null, null)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/collections/{collectionId}/copies/{copyId}", collectionId, copyId)
                        .with(TestJwt.asUser(otherOwnerId)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/collections/{collectionId}/copies/{copyId}", collectionId, copyId)
                        .with(TestJwt.asAdmin(adminId)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/collections/{collectionId}/copies", collectionId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(copyId)));
    }

    @Test
    void publicCollectionShowsOnlyCatalogTitleAndCondition() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID catalogItemId = UUID.randomUUID();
        when(catalogClient.findItem(catalogItemId)).thenReturn(new CatalogItemSummary(catalogItemId, "Libro publico"));
        String publicCollectionId = createCollection(ownerId, "PUBLIC");
        String privateCollectionId = createCollection(ownerId, "PRIVATE");
        String locationId = createLocation(ownerId, "Ubicacion privada");
        createCopy(ownerId, publicCollectionId, copyRequest(catalogItemId, locationId,
                CopyCondition.LIKE_NEW, "2024-05-01", "99.99", "120.00", "Nota privada"));

        mockMvc.perform(get("/api/v1/public/collections/{collectionId}/copies", publicCollectionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].catalogItemId").value(catalogItemId.toString()))
                .andExpect(jsonPath("$[0].catalogItemTitle").value("Libro publico"))
                .andExpect(jsonPath("$[0].condition").value("LIKE_NEW"))
                .andExpect(jsonPath("$[0].locationId").doesNotExist())
                .andExpect(jsonPath("$[0].acquiredOn").doesNotExist())
                .andExpect(jsonPath("$[0].purchasePrice").doesNotExist())
                .andExpect(jsonPath("$[0].currentValue").doesNotExist())
                .andExpect(jsonPath("$[0].notes").doesNotExist());
        mockMvc.perform(get("/api/v1/public/collections/{collectionId}/copies", privateCollectionId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/public/collections/{collectionId}/copies", publicCollectionId)
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validatesCopyDataAndProtectsLinkedLocationDeletion() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID catalogItemId = UUID.randomUUID();
        UUID missingCatalogItemId = UUID.randomUUID();
        when(catalogClient.findItem(catalogItemId)).thenReturn(new CatalogItemSummary(catalogItemId, "Libro"));
        when(catalogClient.findItem(missingCatalogItemId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Elemento no encontrado"));
        String collectionId = createCollection(ownerId, "PRIVATE");
        String locationId = createLocation(ownerId, "Caja");

        String[] invalidRequests = {
                "{}",
                copyRequest(catalogItemId, null, null, "2024-01-01", null, null, null),
                copyRequest(catalogItemId, null, CopyCondition.GOOD, null, null, null, null),
                copyRequest(catalogItemId, null, CopyCondition.GOOD, "2024-01-01", "-0.01", null, null),
                copyRequest(catalogItemId, null, CopyCondition.GOOD, "2024-01-01", "1.999", null, null),
                copyRequest(catalogItemId, null, CopyCondition.GOOD, "2024-01-01", null, null, "N".repeat(2001))
        };
        for (String invalidRequest : invalidRequests) {
            mockMvc.perform(post("/api/v1/collections/{collectionId}/copies", collectionId)
                            .with(TestJwt.asUser(ownerId)).contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }

        mockMvc.perform(post("/api/v1/collections/{collectionId}/copies", collectionId)
                        .with(TestJwt.asUser(ownerId)).contentType(MediaType.APPLICATION_JSON)
                        .content(copyRequest(missingCatalogItemId, null, CopyCondition.GOOD,
                                "2024-01-01", null, null, null)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/collections/{collectionId}/copies", collectionId)
                        .with(TestJwt.asUser(ownerId)).contentType(MediaType.APPLICATION_JSON)
                        .content(copyRequest(catalogItemId, UUID.randomUUID().toString(), CopyCondition.GOOD,
                                "2024-01-01", null, null, null)))
                .andExpect(status().isNotFound());

        createCopy(ownerId, collectionId, copyRequest(catalogItemId, locationId,
                CopyCondition.GOOD, "2024-01-01", null, null, null));
        mockMvc.perform(delete("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/v1/collections/{id}", collectionId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isNoContent());
    }

    @Test
    void requiresAuthenticationForPrivateCopyRoutes() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/collections/{collectionId}/copies", id))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/collections/{collectionId}/copies", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/collections/{collectionId}/copies/{copyId}", id, UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private String createCollection(UUID ownerId, String visibility) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/collections").with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateCollectionRequest("Coleccion de prueba", null,
                                        CollectionVisibility.valueOf(visibility)))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asText();
    }

    private String createLocation(UUID ownerId, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/locations").with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String createCopy(UUID ownerId, String collectionId, String request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/collections/{collectionId}/copies", collectionId)
                        .with(TestJwt.asUser(ownerId)).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith(
                        "/api/v1/collections/" + collectionId + "/copies/")))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String copyRequest(
            UUID catalogItemId,
            String locationId,
            CopyCondition condition,
            String acquiredOn,
            String purchasePrice,
            String currentValue,
            String notes
    ) throws Exception {
        StringBuilder request = new StringBuilder("{");
        if (catalogItemId != null) {
            request.append("\"catalogItemId\":\"").append(catalogItemId).append("\",");
        }
        if (condition != null) {
            request.append("\"condition\":\"").append(condition).append("\",");
        }
        if (locationId != null) {
            request.append("\"locationId\":\"").append(locationId).append("\",");
        }
        if (acquiredOn != null) {
            request.append("\"acquiredOn\":\"").append(acquiredOn).append("\",");
        }
        if (purchasePrice != null) {
            request.append("\"purchasePrice\":").append(purchasePrice).append(',');
        }
        if (currentValue != null) {
            request.append("\"currentValue\":").append(currentValue).append(',');
        }
        if (notes != null) {
            request.append("\"notes\":\"").append(notes).append("\",");
        }
        if (request.length() > 1 && request.charAt(request.length() - 1) == ',') {
            request.setLength(request.length() - 1);
        }
        request.append('}');
        return request.toString();
    }
}