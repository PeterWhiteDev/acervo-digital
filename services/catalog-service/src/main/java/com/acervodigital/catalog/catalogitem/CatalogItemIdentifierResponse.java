package com.acervodigital.catalog.catalogitem;

public record CatalogItemIdentifierResponse(
        IdentifierScheme scheme,
        String value
) {

    static CatalogItemIdentifierResponse from(CatalogItemIdentifier identifier) {
        return new CatalogItemIdentifierResponse(identifier.getScheme(), identifier.getValue());
    }
}