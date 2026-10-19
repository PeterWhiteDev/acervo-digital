package com.acervodigital.identity.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetAdminCollectorPasswordRequest(
        @NotBlank @Size(min = 8, max = 72) String password
) {
}