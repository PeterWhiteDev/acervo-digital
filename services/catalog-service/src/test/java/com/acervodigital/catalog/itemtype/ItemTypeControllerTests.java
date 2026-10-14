package com.acervodigital.catalog.itemtype;

import com.acervodigital.catalog.security.TestJwt;
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

    private static final UUID COLLECTOR_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OTHER_COLLECTOR_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Test
    void listsGlobalTemplates() throws Exception {
        mockMvc.perform(get("/api/v1/item-types").with(TestJwt.asUser(COLLECTOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("Libro", "Pelicula")))
                .andExpect(jsonPath("$[*].scope", hasItems("GLOBAL")));
    }

    @Test
    void createsAndReadsPersonalType() throws Exception {
        String name = "Juegos " + UUID.randomUUID();
        String request = "{\"name\":\"" + name + "\",\"description\":\"Juegos de mesa\"}";

        mockMvc.perform(post("/api/v1/item-types").with(TestJwt.asUser(COLLECTOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/item-types/")))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.scope").value("PERSONAL"));
    }

    @Test
    void rejectsBlankTypeName() throws Exception {
        mockMvc.perform(post("/api/v1/item-types").with(TestJwt.asUser(COLLECTOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/item-types"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void hidesPersonalTypesFromOtherCollectors() throws Exception {
        String name = "Privado " + UUID.randomUUID();
        String location = mockMvc.perform(post("/api/v1/item-types").with(TestJwt.asUser(COLLECTOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");
        String typeId = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(get("/api/v1/item-types/{id}", typeId).with(TestJwt.asUser(OTHER_COLLECTOR_ID)))
                .andExpect(status().isNotFound());
    }
}