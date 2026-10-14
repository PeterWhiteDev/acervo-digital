CREATE TABLE collector_account (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_collector_account_email UNIQUE (email),
    CONSTRAINT ck_collector_account_role CHECK (role IN ('USER', 'ADMIN'))
);