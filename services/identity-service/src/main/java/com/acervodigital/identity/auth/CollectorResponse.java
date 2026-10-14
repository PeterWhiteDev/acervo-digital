package com.acervodigital.identity.auth;

import com.acervodigital.identity.account.CollectorAccount;
import com.acervodigital.identity.account.CollectorRole;

import java.util.UUID;

public record CollectorResponse(
        UUID collectorId,
        String email,
        String displayName,
        CollectorRole role
) {

    public static CollectorResponse from(CollectorAccount account) {
        return new CollectorResponse(
                account.getId(),
                account.getEmail(),
                account.getProfile().getDisplayName(),
                account.getRole()
        );
    }
}