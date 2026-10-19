package com.acervodigital.identity.admin;

import com.acervodigital.identity.account.CollectorAccount;
import com.acervodigital.identity.account.CollectorRole;

import java.time.Instant;
import java.util.UUID;

public record AdminCollectorResponse(
        UUID collectorId,
        String email,
        String displayName,
        CollectorRole role,
        boolean active,
        Instant createdAt
) {

    static AdminCollectorResponse from(CollectorAccount account) {
        return new AdminCollectorResponse(
                account.getId(),
                account.getEmail(),
                account.getProfile().getDisplayName(),
                account.getRole(),
                account.isActive(),
                account.getCreatedAt()
        );
    }
}