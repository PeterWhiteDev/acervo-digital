package com.acervodigital.catalog.attribute;

import com.acervodigital.catalog.itemtype.ItemType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "attribute_definition")
public class AttributeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_type_id", nullable = false)
    private ItemType itemType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "attribute_key", nullable = false, length = 100)
    private String attributeKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private AttributeDataType dataType;

    @Column(nullable = false)
    private boolean required;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected AttributeDefinition() {
    }

    private AttributeDefinition(
            ItemType itemType,
            String name,
            String attributeKey,
            AttributeDataType dataType,
            boolean required,
            int displayOrder
    ) {
        this.itemType = itemType;
        update(name, attributeKey, dataType, required, displayOrder);
    }

    static AttributeDefinition create(
            ItemType itemType,
            String name,
            String attributeKey,
            AttributeDataType dataType,
            boolean required,
            int displayOrder
    ) {
        return new AttributeDefinition(itemType, name, attributeKey, dataType, required, displayOrder);
    }

    void update(String name, String attributeKey, AttributeDataType dataType, boolean required, int displayOrder) {
        this.name = name;
        this.attributeKey = attributeKey;
        this.dataType = dataType;
        this.required = required;
        this.displayOrder = displayOrder;
    }

    public UUID getId() {
        return id;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getName() {
        return name;
    }

    public String getAttributeKey() {
        return attributeKey;
    }

    public AttributeDataType getDataType() {
        return dataType;
    }

    public boolean isRequired() {
        return required;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}