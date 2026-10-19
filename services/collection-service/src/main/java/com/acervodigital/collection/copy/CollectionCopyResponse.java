package com.acervodigital.collection.copy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CollectionCopyResponse(
        UUID id,
        UUID catalogItemId,
        String catalogItemTitle,
        CopyCondition condition,
        UUID locationId,
        LocalDate acquiredOn,
        BigDecimal purchasePrice,
        BigDecimal currentValue,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {

    static CollectionCopyResponse from(CollectionCopy copy, String catalogItemTitle) {
        return new CollectionCopyResponse(
                copy.getId(),
                copy.getCatalogItemId(),
                catalogItemTitle,
                copy.getCondition(),
                copy.getLocation() == null ? null : copy.getLocation().getId(),
                copy.getAcquiredOn(),
                copy.getPurchasePrice(),
                copy.getCurrentValue(),
                copy.getNotes(),
                copy.getCreatedAt(),
                copy.getUpdatedAt()
        );
    }
}