package com.acervodigital.catalog.catalogitem;

import com.acervodigital.catalog.itemtype.ItemType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "catalog_item")
public class CatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_type_id", nullable = false)
    private ItemType itemType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "image_reference", length = 1000)
    private String imageReference;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "catalogItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeValue> attributeValues = new ArrayList<>();

    protected CatalogItem() {
    }

    private CatalogItem(ItemType itemType, String title, String description, UUID createdBy) {
        this.itemType = itemType;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
    }

    static CatalogItem create(ItemType itemType, String title, String description, UUID createdBy) {
        return new CatalogItem(itemType, title, description, createdBy);
    }

    void addAttributeValue(AttributeValue attributeValue) {
        attributeValues.add(attributeValue);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public String getImageReference() {
        return imageReference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<AttributeValue> getAttributeValues() {
        return attributeValues;
    }
}