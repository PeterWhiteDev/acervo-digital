CREATE TABLE item_type (
    id UUID PRIMARY KEY,
    type_scope VARCHAR(20) NOT NULL,
    owner_id UUID,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_item_type_scope CHECK (type_scope IN ('GLOBAL', 'PERSONAL')),
    CONSTRAINT ck_item_type_owner CHECK (
        (type_scope = 'GLOBAL' AND owner_id IS NULL)
        OR (type_scope = 'PERSONAL' AND owner_id IS NOT NULL)
    ),
    CONSTRAINT uq_item_type_owner_name UNIQUE (owner_id, name)
);

INSERT INTO item_type (id, type_scope, owner_id, name, description)
VALUES (CAST('11111111-1111-1111-1111-111111111111' AS UUID), 'GLOBAL', NULL, 'Libro', 'Plantilla global para libros');

INSERT INTO item_type (id, type_scope, owner_id, name, description)
VALUES (CAST('22222222-2222-2222-2222-222222222222' AS UUID), 'GLOBAL', NULL, 'Pelicula', 'Plantilla global para peliculas');