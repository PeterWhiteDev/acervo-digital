package com.acervodigital.catalog.itemtype;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DevelopmentCollectorProvider implements CurrentCollectorProvider {

    private final UUID collectorId;

    public DevelopmentCollectorProvider(@Value("${acervo.catalog.development-collector-id}") UUID collectorId) {
        this.collectorId = collectorId;
    }

    @Override
    public UUID getCurrentCollectorId() {
        return collectorId;
    }
}