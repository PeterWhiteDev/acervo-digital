package com.acervodigital.catalog.attribute;

import com.fasterxml.jackson.databind.JsonNode;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttributeDefinitionControllerTests {

    private static final UUID BOOK_TYPE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listsSeededAttributesForGlobalTemplate() throws Exception {
        mockMvc.perform(get("/api/v1/item-types/{itemTypeId}/attributes", BOOK_TYPE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attributeKey").value("autor"))
                .andExpect(jsonPath("$[0].dataType").value("TEXT"))
                .andExpect(jsonPath("$[1].attributeKey").value("anio_publicacion"))
                .andExpect(jsonPath("$[1].dataType").value("INTEGER"));
    }

    @Test
    void createsAndUpdatesAttributeForPersonalType() throws Exception {
        UUID itemTypeId = createPersonalType();
        String createRequest = """
                {
                  "name": "Aforo",
                  "attributeKey": "aforo",
                  "dataType": "INTEGER",
                  "required": true,
                  "displayOrder": 1
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/v1/item-types/{itemTypeId}/attributes", itemTypeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/attributes/")))
                .andExpect(jsonPath("$.name").value("Aforo"))
                .andExpect(jsonPath("$.dataType").value("INTEGER"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        UUID attributeId = UUID.fromString(created.get("id").asText());

        mockMvc.perform(get("/api/v1/item-types/{itemTypeId}/attributes/{attributeId}", itemTypeId, attributeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attributeKey").value("aforo"));

        String updateRequest = """
                {
                  "name": "Capacidad",
                  "attributeKey": "capacidad",
                  "dataType": "INTEGER",
                  "required": false,
                  "displayOrder": 2
                }
                """;

        mockMvc.perform(put("/api/v1/item-types/{itemTypeId}/attributes/{attributeId}", itemTypeId, attributeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Capacidad"))
                .andExpect(jsonPath("$.attributeKey").value("capacidad"))
                .andExpect(jsonPath("$.required").value(false))
                .andExpect(jsonPath("$.displayOrder").value(2));
    }

    @Test
    void rejectsChangesToGlobalTemplates() throws Exception {
        mockMvc.perform(post("/api/v1/item-types/{itemTypeId}/attributes", BOOK_TYPE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Edicion",
                                  "attributeKey": "edicion",
                                  "dataType": "TEXT",
                                  "required": false,
                                  "displayOrder": 5
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsInvalidAttributeDefinition() throws Exception {
        UUID itemTypeId = createPersonalType();

        mockMvc.perform(post("/api/v1/item-types/{itemTypeId}/attributes", itemTypeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Año",
                                  "attributeKey": "anio",
                                  "dataType": "INTEGER",
                                  "required": true,
                                  "displayOrder": -1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private UUID createPersonalType() throws Exception {
        String typeName = "Juegos " + UUID.randomUUID();
        MvcResult result = mockMvc.perform(post("/api/v1/item-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + typeName + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(created.get("id").asText());
    }
}