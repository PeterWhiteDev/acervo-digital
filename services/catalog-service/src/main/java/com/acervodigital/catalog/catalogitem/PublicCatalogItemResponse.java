package com.acervodigital.catalog.catalogitem;

import java.util.UUID;

public record PublicCatalogItemResponse(UUID id, String title) {

    static PublicCatalogItemResponse from(CatalogItemResponse item) {
        return new PublicCatalogItemResponse(item.id(), item.title());
    }
}