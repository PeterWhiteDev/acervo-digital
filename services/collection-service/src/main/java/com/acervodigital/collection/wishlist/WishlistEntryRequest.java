package com.acervodigital.collection.wishlist;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record WishlistEntryRequest(
        @NotNull UUID catalogItemId,
        @NotNull WishlistPriority priority,
        @Size(max = 2000) String notes
) {
}