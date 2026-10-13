CREATE TABLE catalog_item (
    id UUID PRIMARY KEY,
    item_type_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    created_by UUID NOT NULL,
    image_reference VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_catalog_item_type FOREIGN KEY (item_type_id) REFERENCES item_type (id)
);

CREATE INDEX ix_catalog_item_type_title ON catalog_item (item_type_id, title);

CREATE TABLE attribute_value (
    id UUID PRIMARY KEY,
    catalog_item_id UUID NOT NULL,
    attribute_definition_id UUID NOT NULL,
    value_data VARCHAR(2000) NOT NULL,
    CONSTRAINT uq_attribute_value_item_definition UNIQUE (catalog_item_id, attribute_definition_id),
    CONSTRAINT fk_attribute_value_item FOREIGN KEY (catalog_item_id) REFERENCES catalog_item (id),
    CONSTRAINT fk_attribute_value_definition FOREIGN KEY (attribute_definition_id) REFERENCES attribute_definition (id)
);