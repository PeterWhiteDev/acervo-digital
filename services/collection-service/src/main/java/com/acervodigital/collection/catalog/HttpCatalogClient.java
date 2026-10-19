package com.acervodigital.collection.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class HttpCatalogClient implements CatalogClient {

    private final RestClient restClient;

    public HttpCatalogClient(RestClient catalogRestClient) {
        this.restClient = catalogRestClient;
    }

    @Override
    public CatalogItemSummary findItem(UUID itemId) {
        try {
            CatalogItemSummary item = restClient.get()
                    .uri("/api/v1/public/items/{id}", itemId)
                    .retrieve()
                    .body(CatalogItemSummary.class);
            if (item == null) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalogo sin respuesta valida");
            }
            return item;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Elemento de catalogo no encontrado");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalogo no disponible");
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalogo no disponible");
        }
    }
}