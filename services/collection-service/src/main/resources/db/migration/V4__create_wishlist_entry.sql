CREATE TABLE wishlist_entry (
    id UUID PRIMARY KEY,
    collection_id UUID NOT NULL,
    catalog_item_id UUID NOT NULL,
    priority VARCHAR(10) NOT NULL,
    notes VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_wishlist_entry_collection FOREIGN KEY (collection_id)
        REFERENCES collector_collection (id) ON DELETE CASCADE,
    CONSTRAINT ck_wishlist_entry_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'))
);

CREATE INDEX ix_wishlist_entry_collection ON wishlist_entry (collection_id);
CREATE INDEX ix_wishlist_entry_catalog_item ON wishlist_entry (catalog_item_id);