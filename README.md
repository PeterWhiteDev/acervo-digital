# Acervo Digital

Monorepositorio para una aplicacion de colecciones personales. El nombre es provisional.

## Estructura

- `services/catalog-service`: primera API Spring Boot para el catalogo comun.
- `services/identity-service`: registro de coleccionistas y emisión de tokens JWT.
- `frontend`: reservado para la interfaz Angular.
- `docs`: analisis de requisitos y diseño tecnico.

El proyecto usa Java 17, Spring Boot 3.5.6, Maven, H2 y Flyway. La base H2 local se guarda en el directorio de trabajo desde el que se inicia el servicio. La última release publicada es `v0.2.0`; `v0.3.0` está en preparación.

## Identidad y autenticación

El servicio de identidad escucha en el puerto `8082` y ofrece:

- `POST /api/v1/auth/register`: crea una cuenta con perfil básico y rol `USER`.
- `POST /api/v1/auth/token`: valida email y contraseña y devuelve un JWT HS256 válido durante 15 minutos.

Las rutas del catálogo requieren `Authorization: Bearer <accessToken>`. En desarrollo ambos servicios usan el mismo secreto local de práctica; puede sustituirse mediante `ACERVO_JWT_SECRET`. No uses el valor local fuera de desarrollo.

Los roles no se aceptan en el registro. Las cuentas nuevas reciben `USER`. Para la práctica local, se puede promover una cuenta a `ADMIN` desde la consola H2 de identity-service (`http://localhost:8082/h2-console`):

```sql
UPDATE collector_account SET role = 'ADMIN' WHERE email = 'admin@example.test';
```

Después de cambiar el rol, inicia sesión de nuevo para recibir un token con el claim actualizado. Este mecanismo es solo para desarrollo; no se debe habilitar como procedimiento de producción.

## Tipos de elemento

La primera migracion crea las plantillas globales `Libro` y `Pelicula`. La API permite crear tipos personales y consultar los tipos globales y personales del coleccionista de desarrollo.

- `GET /api/v1/item-types`
- `GET /api/v1/item-types/{id}`
- `POST /api/v1/item-types`

El catálogo obtiene el UUID del coleccionista autenticado desde el claim `sub` del JWT.

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

El contrato OpenAPI 3.0.3 del API actual está en [`services/catalog-service/openapi.yaml`](services/catalog-service/openapi.yaml). Describe las rutas implementadas bajo `/api/v1`, protegidas con bearer JWT emitidos por `identity-service`.

El contrato inicial de identidad está en [`services/identity-service/openapi.yaml`](services/identity-service/openapi.yaml) e incluye registro y emisión de tokens.

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
mvn -pl services/identity-service spring-boot:run
```

El catálogo se inicia en `http://localhost:8081` y el servicio de identidad en `http://localhost:8082`. La consola H2 estará disponible en `/h2-console` durante el desarrollo.