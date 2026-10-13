package com.acervodigital.catalog.attribute;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AttributeDefinitionRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) @Pattern(regexp = "[a-z][a-z0-9_]*") String attributeKey,
        @NotNull AttributeDataType dataType,
        @NotNull Boolean required,
        @NotNull @Min(0) Integer displayOrder
) {
}