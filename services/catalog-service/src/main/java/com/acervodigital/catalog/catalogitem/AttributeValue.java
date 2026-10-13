package com.acervodigital.catalog.catalogitem;

import com.acervodigital.catalog.attribute.AttributeDefinition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "attribute_value")
public class AttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_item_id", nullable = false)
    private CatalogItem catalogItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private AttributeDefinition attributeDefinition;

    @Column(name = "value_data", nullable = false, length = 2000)
    private String value;

    protected AttributeValue() {
    }

    private AttributeValue(CatalogItem catalogItem, AttributeDefinition attributeDefinition, String value) {
        this.catalogItem = catalogItem;
        this.attributeDefinition = attributeDefinition;
        this.value = value;
    }

    static AttributeValue create(CatalogItem catalogItem, AttributeDefinition definition, String value) {
        AttributeValue attributeValue = new AttributeValue(catalogItem, definition, value);
        catalogItem.addAttributeValue(attributeValue);
        return attributeValue;
    }

    public UUID getId() {
        return id;
    }

    public CatalogItem getCatalogItem() {
        return catalogItem;
    }

    public AttributeDefinition getAttributeDefinition() {
        return attributeDefinition;
    }

    public String getValue() {
        return value;
    }
}