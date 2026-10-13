package com.acervodigital.catalog.catalogitem;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogItemControllerTests {

    private static final UUID BOOK_TYPE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsAndReadsCatalogItemWithTypedAttributes() throws Exception {
        String request = """
                {
                  "itemTypeId": "%s",
                  "title": "Libro de prueba",
                  "attributes": {
                    "autor": "Autora de prueba",
                    "anio_publicacion": "2024",
                    "idioma": "Español"
                  }
                }
                """.formatted(BOOK_TYPE_ID);

        MvcResult result = mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Libro de prueba"))
                .andExpect(jsonPath("$.itemTypeName").value("Libro"))
                .andExpect(jsonPath("$.attributes[0].attributeKey").value("autor"))
                .andExpect(jsonPath("$.attributes[1].value").value("2024"))
                .andReturn();

        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID itemId = UUID.fromString(created.get("id").asText());

        mockMvc.perform(get("/api/v1/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId.toString()))
                .andExpect(jsonPath("$.attributes[2].attributeKey").value("idioma"));
    }

    @Test
    void rejectsAttributeValuesThatDoNotMatchTheirDataType() throws Exception {
        String request = """
                {
                  "itemTypeId": "%s",
                  "title": "Libro inválido",
                  "attributes": {
                    "anio_publicacion": "dos mil"
                  }
                }
                """.formatted(BOOK_TYPE_ID);

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}