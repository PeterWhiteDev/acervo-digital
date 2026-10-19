package com.acervodigital.collection.copy;

import java.util.UUID;

public record PublicCollectionCopyResponse(
        UUID id,
        UUID catalogItemId,
        String catalogItemTitle,
        CopyCondition condition
) {

    static PublicCollectionCopyResponse from(CollectionCopy copy, String catalogItemTitle) {
        return new PublicCollectionCopyResponse(
                copy.getId(),
                copy.getCatalogItemId(),
                catalogItemTitle,
                copy.getCondition()
        );
    }
}