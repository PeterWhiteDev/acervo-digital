package com.acervodigital.catalog.itemtype;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemTypeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsGlobalTemplates() throws Exception {
        mockMvc.perform(get("/api/v1/item-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("Libro", "Pelicula")))
                .andExpect(jsonPath("$[*].scope", hasItems("GLOBAL")));
    }

    @Test
    void createsAndReadsPersonalType() throws Exception {
        String name = "Juegos " + UUID.randomUUID();
        String request = "{\"name\":\"" + name + "\",\"description\":\"Juegos de mesa\"}";

        mockMvc.perform(post("/api/v1/item-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/item-types/")))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.scope").value("PERSONAL"));
    }

    @Test
    void rejectsBlankTypeName() throws Exception {
        mockMvc.perform(post("/api/v1/item-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }
}