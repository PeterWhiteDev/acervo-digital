package com.acervodigital.catalog.catalogitem;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record CatalogItemResponse(
        UUID id,
        UUID itemTypeId,
        String itemTypeName,
        String title,
        String description,
        String imageReference,
        Instant createdAt,
        List<CatalogItemAttributeResponse> attributes
) {

    static CatalogItemResponse from(CatalogItem item) {
        List<CatalogItemAttributeResponse> attributes = item.getAttributeValues().stream()
                .sorted(Comparator.comparingInt(value -> value.getAttributeDefinition().getDisplayOrder()))
                .map(CatalogItemAttributeResponse::from)
                .toList();
        return new CatalogItemResponse(
                item.getId(),
                item.getItemType().getId(),
                item.getItemType().getName(),
                item.getTitle(),
                item.getDescription(),
                item.getImageReference(),
                item.getCreatedAt(),
                attributes
        );
    }
}