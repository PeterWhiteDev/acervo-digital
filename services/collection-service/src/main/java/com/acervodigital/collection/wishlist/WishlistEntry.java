package com.acervodigital.collection.wishlist;

import com.acervodigital.collection.collection.CollectionResource;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wishlist_entry")
public class WishlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private CollectionResource collection;

    @Column(name = "catalog_item_id", nullable = false)
    private UUID catalogItemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private WishlistPriority priority;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WishlistEntry() {
    }

    static WishlistEntry create(
            CollectionResource collection,
            UUID catalogItemId,
            WishlistPriority priority,
            String notes
    ) {
        WishlistEntry entry = new WishlistEntry();
        entry.collection = collection;
        entry.update(catalogItemId, priority, notes);
        return entry;
    }

    void update(UUID catalogItemId, WishlistPriority priority, String notes) {
        this.catalogItemId = catalogItemId;
        this.priority = priority;
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

    public WishlistPriority getPriority() {
        return priority;
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