package com.acervodigital.catalog.catalogitem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CatalogItemIdentifierRequest(
        @NotNull IdentifierScheme scheme,
        @NotBlank @Size(max = 120) String value
) {
}