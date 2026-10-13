package com.acervodigital.catalog.itemtype;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "item_type")
public class ItemType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_scope", nullable = false, length = 20)
    private ItemTypeScope scope;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active;

    protected ItemType() {
    }

    private ItemType(String name, String description, UUID ownerId) {
        this.scope = ItemTypeScope.PERSONAL;
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.active = true;
    }

    static ItemType createPersonal(String name, String description, UUID ownerId) {
        return new ItemType(name, description, ownerId);
    }

    public UUID getId() {
        return id;
    }

    public ItemTypeScope getScope() {
        return scope;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}