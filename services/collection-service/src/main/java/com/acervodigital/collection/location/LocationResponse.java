package com.acervodigital.collection.location;

import java.time.Instant;
import java.util.UUID;

public record LocationResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {

    static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getDescription(),
                location.getCreatedAt(),
                location.getUpdatedAt()
        );
    }
}