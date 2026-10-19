package com.acervodigital.collection.admin;

import java.util.UUID;

public record LocationItemCount(
        UUID locationId,
        String locationName,
        long itemCount
) {
}