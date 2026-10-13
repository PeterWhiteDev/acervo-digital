package com.acervodigital.catalog.attribute;

import java.util.UUID;

public record AttributeDefinitionResponse(
        UUID id,
        UUID itemTypeId,
        String name,
        String attributeKey,
        AttributeDataType dataType,
        boolean required,
        int displayOrder
) {

    static AttributeDefinitionResponse from(AttributeDefinition definition) {
        return new AttributeDefinitionResponse(
                definition.getId(),
                definition.getItemType().getId(),
                definition.getName(),
                definition.getAttributeKey(),
                definition.getDataType(),
                definition.isRequired(),
                definition.getDisplayOrder()
        );
    }
}