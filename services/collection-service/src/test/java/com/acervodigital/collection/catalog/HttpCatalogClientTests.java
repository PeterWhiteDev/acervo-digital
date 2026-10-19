package com.acervodigital.collection.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpCatalogClientTests {

    @Test
    void readsPublicSummaryAndMapsCatalogErrors() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://catalog.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        HttpCatalogClient catalogClient = new HttpCatalogClient(builder.build());
        UUID itemId = UUID.randomUUID();
        UUID missingItemId = UUID.randomUUID();
        UUID unavailableItemId = UUID.randomUUID();

        server.expect(requestTo("http://catalog.test/api/v1/public/items/" + itemId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"id\":\"" + itemId + "\",\"title\":\"Ficha compartida\"}",
                        MediaType.APPLICATION_JSON
                ));
        server.expect(requestTo("http://catalog.test/api/v1/public/items/" + missingItemId))
                .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));
        server.expect(requestTo("http://catalog.test/api/v1/public/items/" + unavailableItemId))
                .andRespond(withStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR));

        assertEquals(new CatalogItemSummary(itemId, "Ficha compartida"), catalogClient.findItem(itemId));
        ResponseStatusException notFound = assertThrows(
                ResponseStatusException.class,
                () -> catalogClient.findItem(missingItemId)
        );
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, notFound.getStatusCode());
        ResponseStatusException unavailable = assertThrows(
                ResponseStatusException.class,
                () -> catalogClient.findItem(unavailableItemId)
        );
        assertEquals(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, unavailable.getStatusCode());
        server.verify();
    }
}