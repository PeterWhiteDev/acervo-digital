# Acervo Digital

Monorepositorio para una aplicacion de colecciones personales. El nombre es provisional.

## Estructura

- `services/catalog-service`: primera API Spring Boot para el catalogo comun.
- `frontend`: reservado para la interfaz Angular.
- `docs`: analisis de requisitos y diseño tecnico.

El proyecto usa Java 17, Spring Boot 3.5.6, Maven, H2 y Flyway. La base H2 local se guarda en el directorio de trabajo desde el que se inicia el servicio. La versión Maven en preparación es `0.2.0`; la última release publicada es `v0.1.0`.

## Tipos de elemento

La primera migracion crea las plantillas globales `Libro` y `Pelicula`. La API permite crear tipos personales y consultar los tipos globales y personales del coleccionista de desarrollo.

- `GET /api/v1/item-types`
- `GET /api/v1/item-types/{id}`
- `POST /api/v1/item-types`

Mientras no haya autenticacion, el coleccionista actual usa el UUID de desarrollo configurado en `application.yml`. Este proveedor se sustituira al integrar identidad/JWT.

## Atributos de los tipos

La segunda migracion crea las definiciones de atributos iniciales para `Libro` y `Pelicula`. Las plantillas globales se pueden consultar, pero no modificar desde la API normal. Los tipos personales admiten crear y modificar definiciones.

- `GET /api/v1/item-types/{itemTypeId}/attributes`
- `GET /api/v1/item-types/{itemTypeId}/attributes/{attributeId}`
- `POST /api/v1/item-types/{itemTypeId}/attributes`
- `PUT /api/v1/item-types/{itemTypeId}/attributes/{attributeId}`

Los tipos de dato admitidos son `TEXT`, `INTEGER`, `DECIMAL`, `DATE` y `BOOLEAN`. Cada definicion incluye clave tecnica, obligatoriedad y orden de presentacion.

Ejemplo de alta para un tipo personal:

```json
{
	"name": "Numero de jugadores",
	"attributeKey": "numero_jugadores",
	"dataType": "INTEGER",
	"required": false,
	"displayOrder": 1
}
```

## Elementos del catalogo

El perfil `dev` carga datos de ejemplo mediante Flyway: 17 largometrajes dirigidos por Guy Ritchie, 10 largometrajes de Quentin Tarantino y 10 clasicos de la literatura espanola. Se excluyen cortos, television y segmentos de peliculas de antologia. Los dos volumenes de `Kill Bill` se guardan como fichas distintas. El perfil `test` no carga estos registros.

- `GET /api/v1/items?itemTypeId={id}&size=50`: listar de forma paginada, opcionalmente por tipo.
- `GET /api/v1/items/{id}`: consultar una ficha con sus atributos.
- `POST /api/v1/items`: crear una ficha.
- `PUT /api/v1/items/{id}`: reemplazar el título, la descripción, los identificadores y los atributos de una ficha existente. El tipo de elemento no se modifica.

Para consultar las peliculas de ejemplo, usa `itemTypeId=22222222-2222-2222-2222-222222222222`; para los libros, `itemTypeId=11111111-1111-1111-1111-111111111111`.

Ejemplo de alta de un libro:

```json
{
	"itemTypeId": "11111111-1111-1111-1111-111111111111",
	"title": "Libro de ejemplo",
	"attributes": {
		"autor": "Autora de ejemplo",
		"anio_publicacion": "2024",
		"idioma": "Español"
	}
}
```

La actualización usa los mismos campos editables y reemplaza por completo las listas de identificadores y atributos. Para conservar un identificador existente, inclúyelo en la petición `PUT`.

La filmografia se limita a peliculas dirigidas: [Guy Ritchie](https://en.wikipedia.org/wiki/Guy_Ritchie#Filmography) y [Quentin Tarantino](https://en.wikipedia.org/wiki/List_of_works_by_Quentin_Tarantino). Los datos incluyen titulo, director o autor y ano de estreno o publicacion; no representan una edicion concreta ni incluyen ISBN.

## Comandos

Desde la raiz del proyecto:

```powershell
mvn test
mvn -pl services/catalog-service spring-boot:run
```

La API se iniciara en `http://localhost:8081`. La consola H2 estara disponible en `/h2-console` durante el desarrollo.