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

import java.util.Locale;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                  "identifiers": [
                    { "scheme": "ISBN_10", "value": "0-306-40615-2" },
                    { "scheme": "ISBN_13", "value": "978-0-306-40615-7" }
                  ],
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
                .andExpect(jsonPath("$.identifiers[0].scheme").value("ISBN_10"))
                .andExpect(jsonPath("$.identifiers[0].value").value("0306406152"))
                .andExpect(jsonPath("$.identifiers[1].value").value("9780306406157"))
                .andExpect(jsonPath("$.attributes[0].attributeKey").value("autor"))
                .andExpect(jsonPath("$.attributes[1].value").value("2024"))
                .andReturn();

        JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
        UUID itemId = UUID.fromString(created.get("id").asText());

        mockMvc.perform(get("/api/v1/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId.toString()))
                .andExpect(jsonPath("$.identifiers.length()").value(2))
                .andExpect(jsonPath("$.attributes[2].attributeKey").value("idioma"));

        String editionCode = "edicion-" + UUID.randomUUID();
        String updateRequest = """
          {
            "title": "Libro actualizado",
            "description": "Edición revisada",
            "identifiers": [
              { "scheme": "ISBN_10", "value": "0306406152" },
              { "scheme": "EDITION_CODE", "value": "%s" }
            ],
            "attributes": {
              "autor": "Autora actualizada"
            }
          }
          """.formatted(editionCode);

        mockMvc.perform(put("/api/v1/items/{id}", itemId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateRequest))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value("Libro actualizado"))
          .andExpect(jsonPath("$.description").value("Edición revisada"))
          .andExpect(jsonPath("$.identifiers.length()").value(2))
          .andExpect(jsonPath("$.identifiers[0].scheme").value("EDITION_CODE"))
          .andExpect(jsonPath("$.identifiers[1].scheme").value("ISBN_10"))
          .andExpect(jsonPath("$.attributes.length()").value(1))
          .andExpect(jsonPath("$.attributes[0].value").value("Autora actualizada"));

        mockMvc.perform(get("/api/v1/items/{id}", itemId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value("Libro actualizado"))
          .andExpect(jsonPath("$.identifiers.length()").value(2))
          .andExpect(jsonPath("$.identifiers[0].value").value(editionCode.toUpperCase(Locale.ROOT)));
    }

    @Test
    void rejectsDuplicateNormalizedIdentifiers() throws Exception {
        String request = """
                {
                  "itemTypeId": "%s",
                  "title": "Libro con identificadores duplicados",
                  "identifiers": [
                    { "scheme": "EAN_13", "value": "4006381333931" },
                    { "scheme": "EAN_13", "value": "400-638133393-1" }
                  ],
                  "attributes": {}
                }
                """.formatted(BOOK_TYPE_ID);

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsIdentifierWithInvalidCheckDigit() throws Exception {
        String request = """
                {
                  "itemTypeId": "%s",
                  "title": "Libro con ISBN inválido",
                  "identifiers": [
                    { "scheme": "ISBN_13", "value": "9780306406158" }
                  ],
                  "attributes": {}
                }
                """.formatted(BOOK_TYPE_ID);

        mockMvc.perform(post("/api/v1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
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