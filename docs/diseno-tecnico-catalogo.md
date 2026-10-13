# Diseño técnico inicial: catálogo

## Alcance del primer incremento

Implementar `catalog-service` como primera historia vertical del monorepositorio. Permitirá gestionar tipos configurables y elementos compartidos del catálogo, con sus identificadores, atributos y una imagen principal.

Quedan fuera de este incremento las colecciones, las copias de cada usuario, las listas de deseos, la autenticación real, Angular, el API Gateway y PostgreSQL. Se incorporarán en fases posteriores.

## Decisiones técnicas

- Java 17, Spring Boot 3.5.6 y Maven.
- H2 durante el desarrollo inicial.
- Flyway para crear y evolucionar el esquema desde la primera versión.
- Persistencia relacional con JPA; no almacenar atributos como JSON para facilitar la migración a PostgreSQL.
- DTO para las peticiones y respuestas HTTP; las entidades JPA no se exponen directamente.
- Validación de entradas y errores HTTP con un formato consistente.
- API bajo el prefijo `/api/v1`.

Spring Boot 3.5.6 se fija como versión inicial compatible con Java 17.

## Modelo de dominio propuesto

```text
ItemType 1 ───── * AttributeDefinition
ItemType 1 ───── * CatalogItem
CatalogItem 1 ── * Identifier
CatalogItem 1 ── * AttributeValue
CatalogItem 0..1 ── imagen principal
AttributeDefinition 1 ── * AttributeValue
```

### `ItemType`

Representa un tipo de elemento, por ejemplo `Libro` o `Película`.

- `id`: UUID
- `name`: nombre visible
- `description`: opcional
- `scope`: `GLOBAL` para plantillas compartidas o `PERSONAL` para tipos del coleccionista
- `ownerId`: UUID del coleccionista para tipos personales; nulo para tipos globales
- `active`: permite retirar un tipo sin borrar elementos existentes

### `AttributeDefinition`

Define un atributo aplicable a los elementos de un tipo.

- `id`: UUID
- `itemTypeId`: tipo al que pertenece
- `name`: nombre visible
- `key`: identificador estable para las API
- `valueType`: `TEXT`, `INTEGER`, `DECIMAL`, `DATE` o `BOOLEAN`
- `required`: indica si el atributo debe tener valor
- `displayOrder`: posición del atributo en la presentación

Los valores permitidos para cada tipo se validarán en el servicio. La edición o eliminación de definiciones que ya tengan valores asociados deberá proteger la integridad de los elementos existentes.

### `CatalogItem`

Representa la ficha compartida de un elemento coleccionable, no una copia física de un usuario.

- `id`: UUID
- `itemTypeId`: tipo del elemento
- `title`: título o nombre obligatorio
- `description`: opcional
- `createdBy`: identificador del creador, inicialmente proporcionado por el perfil de desarrollo
- `createdAt` y `updatedAt`
- `imageReference`: referencia opcional a la imagen principal

El estado, la ubicación, la fecha de adquisición, el precio de compra y el valor actual pertenecen a una futura copia privada y no se guardan en `CatalogItem`.

### `Identifier`

Permite asociar varios identificadores opcionales a un elemento, por ejemplo ISBN-10 e ISBN-13.

- `id`: UUID
- `catalogItemId`: elemento asociado
- `scheme`: tipo de identificador, por ejemplo `ISBN-13`
- `value`: valor normalizado

En esta fase no se impondrá unicidad global al valor: esa regla necesita concretar cómo se comparan esquemas y valores.

### `AttributeValue`

Guarda un valor de atributo para un elemento.

- `id`: UUID
- `catalogItemId`: elemento asociado
- `attributeDefinitionId`: definición aplicada
- `value`: representación textual validada según `valueType`

Debe existir como máximo un valor por pareja elemento-definición. Los valores se convierten y validan en la capa de servicio, no se aceptan como texto arbitrario.

## API REST propuesta

### Tipos y definiciones

- `POST /api/v1/item-types`: crear un tipo.
- `GET /api/v1/item-types`: listar tipos activos.
- `GET /api/v1/item-types/{id}`: consultar un tipo.
- `POST /api/v1/item-types/{id}/attributes`: añadir una definición.
- `GET /api/v1/item-types/{id}/attributes`: listar definiciones por orden de presentación.
- `PUT /api/v1/item-types/{id}/attributes/{attributeId}`: modificar una definición.

Las plantillas globales se pueden consultar, pero sus definiciones no se pueden modificar desde la API normal. Cada coleccionista puede administrar las definiciones de sus tipos personales.

### Elementos del catálogo

- `POST /api/v1/items`: crear un elemento y sus valores de atributos.
- `GET /api/v1/items`: listar con paginación y filtro opcional por tipo.
- `GET /api/v1/items/{id}`: consultar la ficha completa.

La ficha guarda los valores dinámicos en `attribute_value`; la API valida cada valor según el tipo declarado en `AttributeDefinition`.

## Validaciones y errores

- Rechazar tipos inexistentes o inactivos al crear un elemento.
- Rechazar atributos que no pertenezcan al tipo del elemento.
- Exigir las definiciones marcadas como obligatorias.
- Validar el formato del valor según `valueType`.
- Rechazar identificadores o valores de atributos duplicados dentro de la misma ficha.
- Responder `400` para entradas inválidas, `404` para recursos inexistentes y `409` para conflictos de integridad.
- Devolver errores con campos estables: `status`, `code`, `message` y, cuando aplique, `fieldErrors`.

## Persistencia y migraciones

Tablas iniciales: `item_type`, `attribute_definition`, `catalog_item`, `identifier` y `attribute_value`.

Las claves serán UUID. Las claves foráneas y restricciones de unicidad se definirán en las migraciones Flyway. Las columnas de fecha y valor decimal usarán tipos relacionales portables; el dinero se expresará en euros y con precisión decimal, no `float`.

La base de datos de desarrollo será H2 persistida en archivo. Las pruebas usarán H2 en memoria. La configuración futura de PostgreSQL se añadirá en otra fase y se verificará con pruebas específicas antes de migrar.

## Pruebas del primer incremento

- Unitarias: validación de valores tipados y reglas de atributos obligatorios.
- Repositorio: guardar y recuperar tipos, elementos, identificadores y atributos relacionados.
- Integración HTTP: crear un tipo, definir atributos, crear un elemento y consultarlo.
- Casos negativos: tipo inexistente, atributo de otro tipo, valor inválido y atributo obligatorio ausente.
- Ejecutar las pruebas con la configuración H2 de test y comprobar que Flyway crea el esquema desde cero.

## Criterios de aceptación

1. Se puede crear un tipo y definir sus atributos.
2. Se puede crear un elemento de ese tipo con varios identificadores y valores tipados.
3. Una consulta devuelve el elemento y sus datos asociados.
4. La API rechaza valores incompatibles y atributos que no correspondan al tipo.
5. El esquema se crea desde cero mediante Flyway y todas las pruebas automatizadas pasan.

## Decisiones pendientes

- Si la imagen se sube al servicio o se gestiona externamente; este diseño solo reserva una referencia.
- El mecanismo temporal exacto para identificar al creador antes de integrar autenticación.
- Si `DELETE` será borrado físico o lógico al existir referencias desde colecciones.
- Si las definiciones admiten valores enumerados, unicidad o reglas adicionales.

## Siguiente paso

Revisar estas decisiones pendientes y ajustar el modelo. Después, generar el esqueleto Maven de `catalog-service` y completar la historia vertical empezando por migración, entidades y pruebas de persistencia.