package com.acervodigital.identity.admin;

import com.acervodigital.identity.account.CollectorRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAdminCollectorRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String displayName,
        @NotNull CollectorRole role,
        @NotNull Boolean active
) {
}