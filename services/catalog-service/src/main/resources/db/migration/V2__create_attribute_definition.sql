CREATE TABLE attribute_definition (
    id UUID PRIMARY KEY,
    item_type_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    attribute_key VARCHAR(100) NOT NULL,
    data_type VARCHAR(20) NOT NULL,
    required BOOLEAN NOT NULL,
    display_order INTEGER NOT NULL,
    CONSTRAINT ck_attribute_definition_data_type CHECK (data_type IN ('TEXT', 'INTEGER', 'DECIMAL', 'DATE', 'BOOLEAN')),
    CONSTRAINT ck_attribute_definition_display_order CHECK (display_order >= 0),
    CONSTRAINT uq_attribute_definition_type_key UNIQUE (item_type_id, attribute_key),
    CONSTRAINT fk_attribute_definition_item_type FOREIGN KEY (item_type_id) REFERENCES item_type (id)
);

INSERT INTO attribute_definition (id, item_type_id, name, attribute_key, data_type, required, display_order)
VALUES (CAST('a1111111-1111-1111-1111-111111111111' AS UUID), CAST('11111111-1111-1111-1111-111111111111' AS UUID), 'Autor', 'autor', 'TEXT', FALSE, 0);

INSERT INTO attribute_definition (id, item_type_id, name, attribute_key, data_type, required, display_order)
VALUES (CAST('a2222222-2222-2222-2222-222222222222' AS UUID), CAST('11111111-1111-1111-1111-111111111111' AS UUID), 'Año de publicación', 'anio_publicacion', 'INTEGER', FALSE, 1);

INSERT INTO attribute_definition (id, item_type_id, name, attribute_key, data_type, required, display_order)
VALUES (CAST('a3333333-3333-3333-3333-333333333333' AS UUID), CAST('11111111-1111-1111-1111-111111111111' AS UUID), 'Editorial', 'editorial', 'TEXT', FALSE, 2);

INSERT INTO attribute_definition (id, item_type_id, name, attribute_key, data_type, required, display_order)
VALUES (CAST('a4444444-4444-4444-4444-444444444444' AS UUID), CAST('11111111-1111-1111-1111-111111111111' AS UUID), 'Idioma', 'idioma', 'TEXT', FALSE, 3);

INSERT INTO attribute_definition (id, item_type_id, name, attribute_key, data_type, required, display_order)
VALUES (CAST('b1111111-1111-1111-1111-111111111111' AS UUID), CAST('22222222-2222-2222-2222-222222222222' AS UUID), 'Director', 'director', 'TEXT', FALSE, 0);

INSERT INTO attribute_definition (id, item_type_id, name, attribute_key, data_type, required, display_order)
VALUES (CAST('b2222222-2222-2222-2222-222222222222' AS UUID), CAST('22222222-2222-2222-2222-222222222222' AS UUID), 'Año de estreno', 'anio_estreno', 'INTEGER', FALSE, 1);

INSERT INTO attribute_definition (id, item_type_id, name, attribute_key, data_type, required, display_order)
VALUES (CAST('b3333333-3333-3333-3333-333333333333' AS UUID), CAST('22222222-2222-2222-2222-222222222222' AS UUID), 'Duración en minutos', 'duracion_minutos', 'INTEGER', FALSE, 2);

INSERT INTO attribute_definition (id, item_type_id, name, attribute_key, data_type, required, display_order)
VALUES (CAST('b4444444-4444-4444-4444-444444444444' AS UUID), CAST('22222222-2222-2222-2222-222222222222' AS UUID), 'Formato', 'formato', 'TEXT', FALSE, 3);