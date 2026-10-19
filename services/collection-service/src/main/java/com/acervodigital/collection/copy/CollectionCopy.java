package com.acervodigital.collection.copy;

import com.acervodigital.collection.collection.CollectionResource;
import com.acervodigital.collection.location.Location;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "collection_copy")
public class CollectionCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private CollectionResource collection;

    @Column(name = "catalog_item_id", nullable = false)
    private UUID catalogItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "copy_condition", nullable = false, length = 20)
    private CopyCondition condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "acquired_on", nullable = false)
    private LocalDate acquiredOn;

    @Column(name = "purchase_price", precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "current_value", precision = 12, scale = 2)
    private BigDecimal currentValue;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CollectionCopy() {
    }

    static CollectionCopy create(
            CollectionResource collection,
            UUID catalogItemId,
            CopyCondition condition,
            Location location,
            LocalDate acquiredOn,
            BigDecimal purchasePrice,
            BigDecimal currentValue,
            String notes
    ) {
        CollectionCopy copy = new CollectionCopy();
        copy.collection = collection;
        copy.update(catalogItemId, condition, location, acquiredOn, purchasePrice, currentValue, notes);
        return copy;
    }

    void update(
            UUID catalogItemId,
            CopyCondition condition,
            Location location,
            LocalDate acquiredOn,
            BigDecimal purchasePrice,
            BigDecimal currentValue,
            String notes
    ) {
        this.catalogItemId = catalogItemId;
        this.condition = condition;
        this.location = location;
        this.acquiredOn = acquiredOn;
        this.purchasePrice = purchasePrice;
        this.currentValue = currentValue;
        this.notes = notes;
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

    public CollectionResource getCollection() {
        return collection;
    }

    public UUID getCatalogItemId() {
        return catalogItemId;
    }

    public CopyCondition getCondition() {
        return condition;
    }

    public Location getLocation() {
        return location;
    }

    public LocalDate getAcquiredOn() {
        return acquiredOn;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}