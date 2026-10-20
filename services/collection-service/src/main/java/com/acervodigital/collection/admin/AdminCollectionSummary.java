package com.acervodigital.collection.admin;

import com.acervodigital.collection.collection.CollectionVisibility;

import java.util.List;
import java.util.UUID;

public record AdminCollectionSummary(
        UUID collectionId,
        String name,
        CollectionVisibility visibility,
        long itemCount,
        List<LocationItemCount> itemsByLocation,
        long unassignedItemCount
) {
}