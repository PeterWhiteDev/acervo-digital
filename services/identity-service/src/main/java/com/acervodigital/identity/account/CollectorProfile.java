package com.acervodigital.identity.account;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CollectorProfile {

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    protected CollectorProfile() {
    }

    private CollectorProfile(String displayName) {
        this.displayName = displayName;
    }

    static CollectorProfile create(String displayName) {
        return new CollectorProfile(displayName);
    }

    public String getDisplayName() {
        return displayName;
    }
}