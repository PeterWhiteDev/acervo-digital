package com.acervodigital.catalog.catalogitem;

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
@Table(name = "catalog_item_identifier")
public class CatalogItemIdentifier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_item_id", nullable = false)
    private CatalogItem catalogItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IdentifierScheme scheme;

    @Column(name = "normalized_value", nullable = false, length = 100)
    private String value;

    protected CatalogItemIdentifier() {
    }

    private CatalogItemIdentifier(CatalogItem catalogItem, IdentifierScheme scheme, String value) {
        this.catalogItem = catalogItem;
        this.scheme = scheme;
        this.value = value;
    }

    static CatalogItemIdentifier create(CatalogItem catalogItem, IdentifierScheme scheme, String value) {
        CatalogItemIdentifier identifier = new CatalogItemIdentifier(catalogItem, scheme, value);
        catalogItem.addIdentifier(identifier);
        return identifier;
    }

    public UUID getId() {
        return id;
    }

    public IdentifierScheme getScheme() {
        return scheme;
    }

    public String getValue() {
        return value;
    }
}