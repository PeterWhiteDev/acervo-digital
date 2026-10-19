# Acervo Digital

Monorepositorio para una aplicacion de colecciones personales. El nombre es provisional.

## Estructura

- `services/catalog-service`: primera API Spring Boot para el catalogo comun.
- `services/identity-service`: registro de coleccionistas y emisión de tokens JWT.
- `services/collection-service`: colecciones, ubicaciones privadas y copias de los usuarios.
- `frontend`: interfaz Angular 22 para gestionar catálogo, tipos, colecciones, ejemplares y deseos.
- `docs`: analisis de requisitos y diseño tecnico.

La fuente Mermaid editable del diagrama de clases del dominio está en [`docs/modelo-clases.mmd`](docs/modelo-clases.mmd); distingue entidades actuales y las previstas en requisitos.

La guía breve para instalar requisitos y ejecutar los servicios está en [`docs/instalacion-y-ejecucion.md`](docs/instalacion-y-ejecucion.md).

El backend usa Java 17, Spring Boot 3.5.6, Maven, H2 y Flyway. La interfaz usa Angular 22 y Node.js 26. Cada servicio mantiene su propia base H2 local. La última release publicada es `v0.3.0`; `0.5.0` y `0.6.0` están en desarrollo.

## Identidad y autenticación

El servicio de identidad escucha en el puerto `8082` y ofrece:

- `POST /api/v1/auth/register`: crea una cuenta con perfil básico y rol `USER`.
- `POST /api/v1/auth/token`: valida email y contraseña y devuelve un JWT HS256 válido durante 15 minutos.

Las rutas de gestión del catálogo requieren `Authorization: Bearer <accessToken>`. La única lectura anónima nueva devuelve solo UUID y título de una ficha. En desarrollo ambos servicios usan el mismo secreto local de práctica; puede sustituirse mediante `ACERVO_JWT_SECRET`. No uses el valor local fuera de desarrollo.

Los roles no se aceptan en el registro. Las cuentas nuevas reciben `USER`. Para la práctica local, se puede promover una cuenta a `ADMIN` desde la consola H2 de identity-service (`http://localhost:8082/h2-console`):

```sql
UPDATE collector_account SET role = 'ADMIN' WHERE email = 'admin@example.test';
```

Después de cambiar el rol, inicia sesión de nuevo para recibir un token con el claim actualizado. Este mecanismo es solo para desarrollo; no se debe habilitar como procedimiento de producción.

El `collection-service` escucha en el puerto `8083` y consulta `catalog-service` en `8081` para validar las fichas asociadas. La URL del catálogo puede configurarse con `ACERVO_CATALOG_URL`.

## Interfaz web

La aplicación Angular está en `frontend`. Para iniciarla, arranca identity-service (`8082`), catalog-service (`8081`) y collection-service (`8083`), y después ejecuta:

```powershell
cd frontend
npm start
```

El servidor local escucha en `http://localhost:4200` y usa `proxy.conf.json` para dirigir autenticación, catálogo y colecciones a sus respectivos servicios. La sesión requiere iniciar sesión o registrarse. El JWT se añade automáticamente a las operaciones protegidas.

La interfaz permite consultar y crear/editar fichas, filtrar el catálogo por tipo, crear tipos personales y atributos, administrar colecciones y ubicaciones, y gestionar copias y deseos, incluida su conversión a ejemplar.

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

El contrato OpenAPI 3.0.3 del API actual está en [`services/catalog-service/openapi.yaml`](services/catalog-service/openapi.yaml). Las rutas de gestión usan bearer JWT emitido por `identity-service`; `GET /api/v1/public/items/{id}` expone anónimamente solo UUID y título.

El contrato inicial de identidad está en [`services/identity-service/openapi.yaml`](services/identity-service/openapi.yaml) e incluye registro y emisión de tokens.

El contrato de colecciones está en [`services/collection-service/openapi.yaml`](services/collection-service/openapi.yaml). El servicio escucha en el puerto `8083`.

El CRUD inicial permite gestionar las colecciones del propietario y consultar colecciones públicas:

- `GET /api/v1/collections`: listar las colecciones propias (JWT requerido).
- `POST /api/v1/collections`: crear una colección propia (JWT requerido).
- `GET /api/v1/collections/{id}`: consultar una colección propia o como `ADMIN` (JWT requerido).
- `PUT /api/v1/collections/{id}` y `DELETE /api/v1/collections/{id}`: modificar o eliminar una colección propia o como `ADMIN` (JWT requerido).
- `GET /api/v1/public/collections` y `GET /api/v1/public/collections/{id}`: consultar solo colecciones `PUBLIC`, sin autenticación.

Las ubicaciones se gestionan en el mismo servicio, siempre con JWT y sin rutas públicas:

- `GET /api/v1/locations`: listar ubicaciones propias ordenadas por nombre, también para `ADMIN`.
- `POST /api/v1/locations`: crear una ubicación para el usuario autenticado; devuelve `201` y cabecera `Location`.
- `GET /api/v1/locations/{id}`: consultar una ubicación propia o como `ADMIN`.
- `PUT /api/v1/locations/{id}`: reemplazar nombre y descripción de una ubicación propia o como `ADMIN`.
- `DELETE /api/v1/locations/{id}`: eliminar una ubicación propia o como `ADMIN`; devuelve `204`.

El nombre es obligatorio (máximo 100 caracteres); la descripción es opcional (máximo 1000). Se recortan espacios extremos. Omitir la descripción en `PUT` la elimina. El propietario se obtiene del JWT y no puede cambiarse mediante estas peticiones.

Ejemplo de alta de ubicación:

```json
{
	"name": "Estantería del salón",
	"description": "Balda superior"
}
```

Flyway V2 de `collection-service` crea `collector_location`. Flyway V3 crea `collection_copy`; al borrar una colección se eliminan sus copias, y no se puede borrar una ubicación asignada hasta desvincularla o borrar las copias.

Las copias admiten varias instancias de la misma ficha de catálogo. La fecha de adquisición y estado son obligatorios; ubicación, precios en euros y notas son opcionales. Los precios no aceptan valores negativos y admiten hasta dos decimales.

- `GET /api/v1/collections/{collectionId}/copies`: listar copias propias (JWT).
- `POST /api/v1/collections/{collectionId}/copies`: crear copia validando que exista la ficha de catálogo.
- `GET`, `PUT` y `DELETE /api/v1/collections/{collectionId}/copies/{copyId}`: consultar, reemplazar o eliminar una copia propia.
- `GET /api/v1/public/collections/{collectionId}/copies`: en una colección pública muestra solo título de catálogo y estado; oculta ubicación, fecha, precios y notas.

Las listas de deseos son privadas y cada colección tiene su propia lista. Una entrada referencia una ficha de catálogo existente y guarda prioridad (`LOW`, `MEDIUM`, `HIGH`) y notas opcionales:

- `GET` y `POST /api/v1/collections/{collectionId}/wishlist`: listar y añadir deseos.
- `GET`, `PUT` y `DELETE /api/v1/collections/{collectionId}/wishlist/{entryId}`: consultar, reemplazar o eliminar un deseo.
- `POST /api/v1/collections/{collectionId}/wishlist/{entryId}/convert`: crear una copia y eliminar el deseo en una transacción.

La conversión requiere condición y fecha de adquisición; ubicación y precios son opcionales. Si no se envían notas nuevas, se conservan las del deseo. Flyway V4 crea las entradas y las elimina al borrar la colección.

Ejemplo de alta de copia:

```json
{
	"catalogItemId": "UUID-DE-UNA-FICHA-EXISTENTE",
	"condition": "GOOD",
	"locationId": "UUID-DE-UNA-UBICACION-PROPIA",
	"acquiredOn": "2026-10-14",
	"purchasePrice": 12.50,
	"currentValue": 15.00,
	"notes": "Primera edición"
}
```

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
mvn -pl services/collection-service spring-boot:run
```

El catálogo se inicia en `http://localhost:8081`, identidad en `http://localhost:8082` y colecciones en `http://localhost:8083`. La consola H2 estará disponible en `/h2-console` durante el desarrollo.