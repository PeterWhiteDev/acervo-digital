package com.acervodigital.collection.collection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCollectionRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 1000) String description,
        @NotNull CollectionVisibility visibility
) {
}