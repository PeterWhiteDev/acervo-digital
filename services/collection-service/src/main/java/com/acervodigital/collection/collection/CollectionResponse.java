package com.acervodigital.collection.collection;

import java.time.Instant;
import java.util.UUID;

public record CollectionResponse(
        UUID id,
        String name,
        String description,
        CollectionVisibility visibility,
        Instant createdAt,
        Instant updatedAt
) {

    static CollectionResponse from(CollectionResource collection) {
        return new CollectionResponse(
                collection.getId(),
                collection.getName(),
                collection.getDescription(),
                collection.getVisibility(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }
}