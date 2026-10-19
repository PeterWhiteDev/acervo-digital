package com.acervodigital.collection.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLocationRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 1000) String description
) {
}