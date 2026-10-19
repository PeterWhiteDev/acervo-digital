package com.acervodigital.collection.wishlist;

import java.time.Instant;
import java.util.UUID;

public record WishlistEntryResponse(
        UUID id,
        UUID catalogItemId,
        String catalogItemTitle,
        WishlistPriority priority,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {

    static WishlistEntryResponse from(WishlistEntry entry, String catalogItemTitle) {
        return new WishlistEntryResponse(
                entry.getId(),
                entry.getCatalogItemId(),
                catalogItemTitle,
                entry.getPriority(),
                entry.getNotes(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }
}