CREATE TABLE catalog_item_identifier (
    id UUID PRIMARY KEY,
    catalog_item_id UUID NOT NULL,
    scheme VARCHAR(30) NOT NULL,
    normalized_value VARCHAR(100) NOT NULL,
    CONSTRAINT uq_catalog_item_identifier UNIQUE (scheme, normalized_value),
    CONSTRAINT fk_catalog_item_identifier_item FOREIGN KEY (catalog_item_id) REFERENCES catalog_item (id)
);

CREATE INDEX ix_catalog_item_identifier_item ON catalog_item_identifier (catalog_item_id);