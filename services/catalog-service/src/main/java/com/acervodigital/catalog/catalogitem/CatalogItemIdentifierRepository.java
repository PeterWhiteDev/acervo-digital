package com.acervodigital.catalog.catalogitem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CatalogItemIdentifierRepository extends JpaRepository<CatalogItemIdentifier, UUID> {

    boolean existsBySchemeAndValue(IdentifierScheme scheme, String value);

    boolean existsBySchemeAndValueAndCatalogItem_IdNot(IdentifierScheme scheme, String value, UUID catalogItemId);
}