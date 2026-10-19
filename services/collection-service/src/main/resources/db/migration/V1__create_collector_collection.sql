CREATE TABLE collector_collection (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    visibility VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_collector_collection_visibility CHECK (visibility IN ('PRIVATE', 'PUBLIC'))
);

CREATE INDEX ix_collector_collection_owner ON collector_collection (owner_id);