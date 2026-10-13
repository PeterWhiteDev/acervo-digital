package com.acervodigital.catalog.catalogitem;

import com.acervodigital.catalog.attribute.AttributeDataType;

public record CatalogItemAttributeResponse(
        String name,
        String attributeKey,
        AttributeDataType dataType,
        String value
) {

    static CatalogItemAttributeResponse from(AttributeValue attributeValue) {
        return new CatalogItemAttributeResponse(
                attributeValue.getAttributeDefinition().getName(),
                attributeValue.getAttributeDefinition().getAttributeKey(),
                attributeValue.getAttributeDefinition().getDataType(),
                attributeValue.getValue()
        );
    }
}