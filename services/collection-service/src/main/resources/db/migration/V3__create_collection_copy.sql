CREATE TABLE collection_copy (
    id UUID PRIMARY KEY,
    collection_id UUID NOT NULL,
    catalog_item_id UUID NOT NULL,
    copy_condition VARCHAR(20) NOT NULL,
    location_id UUID,
    acquired_on DATE NOT NULL,
    purchase_price DECIMAL(12, 2),
    current_value DECIMAL(12, 2),
    notes VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_collection_copy_collection FOREIGN KEY (collection_id)
        REFERENCES collector_collection (id) ON DELETE CASCADE,
    CONSTRAINT fk_collection_copy_location FOREIGN KEY (location_id)
        REFERENCES collector_location (id) ON DELETE RESTRICT,
    CONSTRAINT ck_collection_copy_condition
        CHECK (copy_condition IN ('NEW', 'LIKE_NEW', 'GOOD', 'ACCEPTABLE', 'POOR')),
    CONSTRAINT ck_collection_copy_purchase_price CHECK (purchase_price IS NULL OR purchase_price >= 0),
    CONSTRAINT ck_collection_copy_current_value CHECK (current_value IS NULL OR current_value >= 0)
);

CREATE INDEX ix_collection_copy_collection ON collection_copy (collection_id);
CREATE INDEX ix_collection_copy_catalog_item ON collection_copy (catalog_item_id);
CREATE INDEX ix_collection_copy_location ON collection_copy (location_id);