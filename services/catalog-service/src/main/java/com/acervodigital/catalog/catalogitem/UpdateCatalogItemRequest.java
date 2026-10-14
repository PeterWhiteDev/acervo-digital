package com.acervodigital.catalog.catalogitem;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record UpdateCatalogItemRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        List<@Valid CatalogItemIdentifierRequest> identifiers,
        Map<String, String> attributes
) {
}