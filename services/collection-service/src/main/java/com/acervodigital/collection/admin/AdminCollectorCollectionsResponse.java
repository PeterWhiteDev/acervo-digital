package com.acervodigital.collection.admin;

import java.util.List;
import java.util.UUID;

public record AdminCollectorCollectionsResponse(
        UUID collectorId,
        long collectionCount,
        List<AdminCollectionSummary> collections
) {
}