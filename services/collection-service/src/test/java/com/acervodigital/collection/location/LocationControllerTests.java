package com.acervodigital.collection.location;

import com.acervodigital.collection.security.TestJwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
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
class LocationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void requiresAuthenticationForEveryLocationOperation() throws Exception {
        UUID locationId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/locations"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/locations/{id}", locationId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("Estanteria", null)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/v1/locations/{id}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("Caja", null)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/locations/{id}", locationId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/public/locations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ownerCanCreateReadReplaceAndDeleteLocation() throws Exception {
        UUID ownerId = UUID.randomUUID();
        String locationId = createLocation(ownerId, "  Estanteria  ", "  Salon  ");

        mockMvc.perform(get("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Estanteria"))
                .andExpect(jsonPath("$.description").value("Salon"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
        mockMvc.perform(get("/api/v1/locations").with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(locationId)));

        mockMvc.perform(put("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("  Caja  ", "  Armario  ")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Caja"))
                .andExpect(jsonPath("$.description").value("Armario"));
        mockMvc.perform(get("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Caja"))
                .andExpect(jsonPath("$.description").value("Armario"));

        mockMvc.perform(put("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Caja\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(nullValue()));
        mockMvc.perform(get("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(nullValue()));

        mockMvc.perform(delete("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/locations").with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(not(hasItem(locationId))));
    }

    @Test
    void anotherCollectorCannotReadChangeOrDeleteLocation() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID otherOwnerId = UUID.randomUUID();
        String locationId = createLocation(ownerId, "Biblioteca", null);
        String otherLocationId = createLocation(otherOwnerId, "Otra biblioteca", null);

        mockMvc.perform(get("/api/v1/locations").with(TestJwt.asUser(otherOwnerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(otherLocationId)))
                .andExpect(jsonPath("$[*].id").value(not(hasItem(locationId))));
        mockMvc.perform(get("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(otherOwnerId)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(otherOwnerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("No autorizado", null)))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(otherOwnerId)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Biblioteca"));
    }

    @Test
    void adminCanManageLocationWithoutChangingOwnership() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        String locationId = createLocation(ownerId, "Almacen", null);

        mockMvc.perform(get("/api/v1/locations/{id}", locationId).with(TestJwt.asAdmin(adminId)))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/locations/{id}", locationId).with(TestJwt.asAdmin(adminId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request("Almacen corregido", null)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/locations").with(TestJwt.asAdmin(adminId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(not(hasItem(locationId))));
        mockMvc.perform(get("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Almacen corregido"));
        mockMvc.perform(delete("/api/v1/locations/{id}", locationId).with(TestJwt.asAdmin(adminId)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidDataAndMissingLocations() throws Exception {
        UUID ownerId = UUID.randomUUID();
        String locationId = createLocation(ownerId, "N".repeat(100), "D".repeat(1000));
        String[] invalidRequests = {
                "{}",
                request("   ", null),
                request("N".repeat(101), null),
                request("Nombre", "D".repeat(1001))
        };
        for (String invalidRequest : invalidRequests) {
            mockMvc.perform(post("/api/v1/locations").with(TestJwt.asUser(ownerId))
                            .contentType(MediaType.APPLICATION_JSON).content(invalidRequest))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(put("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId))
                            .contentType(MediaType.APPLICATION_JSON).content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
        mockMvc.perform(get("/api/v1/locations/{id}", locationId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("N".repeat(100)))
                .andExpect(jsonPath("$.description").value("D".repeat(1000)));

        UUID missingId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/locations/{id}", missingId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/v1/locations/{id}", missingId).with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON).content(request("Nombre", null)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/v1/locations/{id}", missingId).with(TestJwt.asUser(ownerId)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/locations/not-a-uuid").with(TestJwt.asUser(ownerId)))
                .andExpect(status().isBadRequest());
    }

    private String createLocation(UUID ownerId, String name, String description) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/locations").with(TestJwt.asUser(ownerId))
                        .contentType(MediaType.APPLICATION_JSON).content(request(name, description)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/locations/")))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String request(String name, String description) throws Exception {
        return objectMapper.writeValueAsString(new CreateLocationRequest(name, description));
    }
}