package com.acervodigital.collection.wishlist;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
class WishlistControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogClient catalogClient;

    @Test
    void ownerCanCreateReadUpdateAndDeleteWishlistEntries() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID catalogItemId = UUID.randomUUID();
        UUID replacementItemId = UUID.randomUUID();
        when(catalogClient.findItem(catalogItemId)).thenReturn(new CatalogItemSummary(catalogItemId, "Libro deseado"));
        when(catalogClient.findItem(replacementItemId))
                .thenReturn(new CatalogItemSummary(replacementItemId, "Otro libro"));
        String collectionId = createCollection(ownerId);

        String entryId = createEntry(ownerId, collectionId, new WishlistEntryRequest(
                catalogItemId, WishlistPriority.MEDIUM, "  Buscar primera edicion  "));
        mockMvc.perform(get("/api/v1/collections/{collectionId}/wishlist", collectionId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(entryId)))
                .andExpect(jsonPath("$[0].catalogItemTitle").value("Libro deseado"))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"))
                .andExpect(jsonPath("$[0].notes").value("Buscar primera edicion"));

        mockMvc.perform(put("/api/v1/collections/{collectionId}/wishlist/{entryId}", collectionId, entryId)
                        .with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WishlistEntryRequest(
                                replacementItemId, WishlistPriority.HIGH, "Regalo"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.catalogItemId").value(replacementItemId.toString()))
                .andExpect(jsonPath("$.catalogItemTitle").value("Otro libro"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.notes").value("Regalo"));

        mockMvc.perform(delete("/api/v1/collections/{collectionId}/wishlist/{entryId}", collectionId, entryId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/collections/{collectionId}/wishlist/{entryId}", collectionId, entryId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void conversionCreatesCopyAndRemovesWishUsingOneRequest() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID catalogItemId = UUID.randomUUID();
        when(catalogClient.findItem(catalogItemId)).thenReturn(new CatalogItemSummary(catalogItemId, "Libro adquirido"));
        String collectionId = createCollection(ownerId);
        String entryId = createEntry(ownerId, collectionId, new WishlistEntryRequest(
                catalogItemId, WishlistPriority.HIGH, "Nota del deseo"));
        ConvertWishlistEntryRequest conversion = new ConvertWishlistEntryRequest(
                CopyCondition.GOOD,
                null,
                LocalDate.of(2026, 10, 19),
                new BigDecimal("12.50"),
                new BigDecimal("15.00"),
                null
        );

        MvcResult result = mockMvc.perform(post(
                                "/api/v1/collections/{collectionId}/wishlist/{entryId}/convert", collectionId, entryId)
                        .with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conversion)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith(
                        "/api/v1/collections/" + collectionId + "/copies/")))
                .andExpect(jsonPath("$.catalogItemId").value(catalogItemId.toString()))
                .andExpect(jsonPath("$.condition").value("GOOD"))
                .andExpect(jsonPath("$.notes").value("Nota del deseo"))
                .andReturn();
        String copyId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/collections/{collectionId}/wishlist", collectionId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/v1/collections/{collectionId}/copies/{copyId}", collectionId, copyId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.catalogItemTitle").value("Libro adquirido"))
                .andExpect(jsonPath("$.purchasePrice").value(12.5));
    }

    @Test
    void failedConversionLeavesWishlistEntryUnchanged() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID catalogItemId = UUID.randomUUID();
        when(catalogClient.findItem(catalogItemId)).thenReturn(new CatalogItemSummary(catalogItemId, "Libro"));
        String collectionId = createCollection(ownerId);
        String entryId = createEntry(ownerId, collectionId, new WishlistEntryRequest(
                catalogItemId, WishlistPriority.LOW, null));
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Elemento no encontrado"))
                .when(catalogClient).findItem(catalogItemId);

        mockMvc.perform(post("/api/v1/collections/{collectionId}/wishlist/{entryId}/convert", collectionId, entryId)
                        .with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConvertWishlistEntryRequest(
                                CopyCondition.NEW, null, LocalDate.of(2026, 10, 19), null, null, null))))
                .andExpect(status().isNotFound());

        doReturn(new CatalogItemSummary(catalogItemId, "Libro"))
                .when(catalogClient).findItem(catalogItemId);
        mockMvc.perform(get("/api/v1/collections/{collectionId}/wishlist/{entryId}", collectionId, entryId)
                        .with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entryId));
    }

    @Test
    void preventsAccessToAnotherCollectorsWishlistAndValidatesRequests() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherOwnerId = UUID.randomUUID();
        UUID catalogItemId = UUID.randomUUID();
        when(catalogClient.findItem(catalogItemId)).thenReturn(new CatalogItemSummary(catalogItemId, "Juego"));
        String collectionId = createCollection(ownerId);
        String entryId = createEntry(ownerId, collectionId, new WishlistEntryRequest(
                catalogItemId, WishlistPriority.MEDIUM, null));

        mockMvc.perform(get("/api/v1/collections/{collectionId}/wishlist", collectionId)
                        .with(TestJwt.asUser(otherOwnerId)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/collections/{collectionId}/wishlist/{entryId}", collectionId, entryId)
                        .with(TestJwt.asUser(otherOwnerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WishlistEntryRequest(
                                catalogItemId, WishlistPriority.HIGH, null))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/collections/{collectionId}/wishlist", collectionId)
                        .with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/collections/{collectionId}/wishlist", collectionId))
                .andExpect(status().isUnauthorized());
    }

    private String createCollection(UUID ownerId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/collections").with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCollectionRequest(
                                "Coleccion de prueba", null, CollectionVisibility.PRIVATE))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("id").asText();
    }

    private String createEntry(UUID ownerId, String collectionId, WishlistEntryRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/collections/{collectionId}/wishlist", collectionId)
                        .with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith(
                        "/api/v1/collections/" + collectionId + "/wishlist/")))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}