package com.acervodigital.collection.security;

import java.util.UUID;

public interface CurrentCollectorProvider {

    UUID getCurrentCollectorId();

    boolean isAdmin();
}