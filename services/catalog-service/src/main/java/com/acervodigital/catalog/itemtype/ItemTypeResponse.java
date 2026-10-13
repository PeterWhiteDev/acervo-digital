package com.acervodigital.catalog.itemtype;

import java.util.UUID;

public record ItemTypeResponse(
        UUID id,
        String name,
        String description,
        ItemTypeScope scope,
        boolean active
) {

    static ItemTypeResponse from(ItemType itemType) {
        return new ItemTypeResponse(
                itemType.getId(),
                itemType.getName(),
                itemType.getDescription(),
                itemType.getScope(),
                itemType.isActive()
        );
    }
}