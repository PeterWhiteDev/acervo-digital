# Analisis de requisitos: Gestor de colecciones

## 1. Introduccion

El sistema permitira a distintos coleccionistas registrar y organizar colecciones de elementos heterogeneos, como libros, peliculas, juegos de mesa, videojuegos o juegos de cartas.

La aplicacion se diseñara de forma generica para admitir nuevos tipos de elementos y atributos configurables sin modificar el modelo principal. Contara con una API REST basada en microservicios y, posteriormente, con una interfaz web desarrollada en Angular.

## 2. Objetivos

- Gestionar varios coleccionistas de manera independiente.
- Permitir que cada coleccionista cree y administre varias colecciones.
- Admitir distintos tipos de elementos dentro de una misma coleccion.
- Reutilizar la informacion de elementos mediante un catalogo comun.
- Mantener privadas las copias y los datos particulares de cada coleccionista.
- Permitir tipos y atributos configurables.
- Gestionar una lista de deseos por coleccion.
- Ofrecer operaciones CRUD mediante una API REST.
- Disponer posteriormente de una interfaz web Angular.
- Servir como proyecto práctico para aprender Spring Boot, microservicios, persistencia y bases de datos, seguridad, pruebas, Git/GitHub, gestión de ramas, versionado, releases y prácticas DevOps.

## 3. Actores

### 3.1 Visitante

- Puede consultar colecciones marcadas como publicas.
- No puede modificar ningun dato.

### 3.2 Coleccionista

- Es un usuario autenticado de la aplicacion.
- Administra un unico perfil personal.
- Puede crear y administrar varias colecciones.
- Puede crear elementos en el catalogo comun.
- Puede administrar sus copias y listas de deseos.
- Decide la visibilidad de cada una de sus colecciones.

### 3.3 Administrador

- Puede administrar usuarios y perfiles.
- Puede corregir o eliminar elementos del catalogo comun cuando las reglas de integridad lo permitan.
- Puede administrar las plantillas globales de tipos.
- Puede acceder a los recursos con fines de administracion.
- No representa un colaborador de las colecciones.

## 4. Alcance funcional del MVP

### 4.1 Usuarios y perfiles

- Cada usuario representa a un unico coleccionista.
- Cada usuario administra un unico perfil.
- Un coleccionista puede administrar varias colecciones.
- Se contemplan los roles `USER` y `ADMIN`.
- La autenticacion con JWT se incorporara en una fase posterior.

### 4.2 Colecciones

- Un coleccionista puede crear, consultar, modificar y eliminar sus colecciones.
- Una coleccion puede contener elementos de diferentes tipos.
- Ejemplo: una coleccion denominada "Juegos" puede contener juegos de mesa, juegos de PS3, juegos de Nintendo y juegos de cartas.
- Cada coleccion tendra un nombre, una descripcion opcional y una visibilidad.
- Las visibilidades iniciales seran:
  - `PRIVATE`: solo el propietario y los administradores pueden consultarla.
  - `PUBLIC`: otros usuarios y visitantes pueden consultarla en modo de solo lectura.
- Solo el propietario y los administradores pueden modificar una coleccion.
- No existiran colaboradores, comentarios, seguidores ni otras funciones sociales.

### 4.3 Catalogo comun

- La informacion general de una obra o producto se almacenara una sola vez en un catalogo comun.
- Distintos coleccionistas podran asociar copias privadas al mismo elemento del catalogo.
- Un usuario podra crear elementos nuevos en el catalogo.
- Un usuario solo podra modificar los elementos creados por el, salvo que tenga rol de administrador.
- Un elemento del catalogo no podra eliminarse si existen copias o deseos asociados.
- Los elementos podran existir sin identificador.
- Un elemento podra tener varios identificadores.
- Ejemplos de identificadores: ISBN-10, ISBN-13, EAN, UPC o un codigo de edicion.

### 4.4 Tipos de elemento

- El sistema admitira tipos configurables.
- Se incluiran inicialmente plantillas globales para `Libro` y `Pelicula`.
- Las plantillas globales no podran ser modificadas por usuarios normales.
- Cada coleccionista podra crear sus propios tipos de elemento.
- Cada tipo definira sus atributos disponibles y cuales son obligatorios.
- Ejemplos de tipos futuros: comic, disco, videojuego, juego de mesa o juego de cartas.

### 4.5 Atributos configurables

- Los atributos especificos se almacenaran mediante un modelo relacional portable entre H2 y PostgreSQL.
- Cada definicion de atributo indicara como minimo:
  - Nombre.
  - Tipo de dato.
  - Obligatoriedad.
  - Orden de presentacion.
- Tipos de dato iniciales:
  - Texto.
  - Numero entero.
  - Numero decimal.
  - Fecha.
  - Booleano.
- Ejemplos para libros: autor, año de publicacion, editorial e idioma.
- Ejemplos para peliculas: director, año de estreno, duracion y formato.

### 4.6 Categorias y autores

- Los elementos podran clasificarse mediante categorias.
- Las categorias no se utilizaran como sustituto de los tipos o atributos.
- Los creadores, como autores o directores, formaran parte de la informacion del catalogo.
- No se incluiran etiquetas libres en el MVP.

### 4.7 Copias privadas

- Una copia representa un ejemplar concreto que pertenece a un coleccionista.
- Un coleccionista podra tener varias copias del mismo elemento del catalogo.
- Cada copia pertenecera a una unica coleccion.
- Cada copia almacenara, como minimo:
  - Elemento del catalogo asociado.
  - Estado de conservacion.
  - Ubicacion.
  - Fecha de adquisicion.
  - Precio de compra en euros.
  - Valor actual en euros.
  - Notas opcionales.
- Estados iniciales:
  - `NEW`.
  - `LIKE_NEW`.
  - `GOOD`.
  - `ACCEPTABLE`.
  - `POOR`.
- No se mantendra un historial de precios o valoraciones.
- No se gestionaran prestamos.

### 4.8 Ubicaciones

- Cada copia podra indicar una ubicacion.
- La ubicacion podra representar una habitacion, estanteria, caja u otra localizacion fisica.
- Las ubicaciones perteneceran al coleccionista y no seran compartidas con otros usuarios.

### 4.9 Lista de deseos

- Cada coleccion tendra su propia lista de deseos.
- Un deseo hara referencia a un elemento del catalogo.
- Al crear un deseo se podra seleccionar un elemento existente o registrar uno nuevo en el catalogo.
- Cada deseo tendra una prioridad.
- Prioridades iniciales: `LOW`, `MEDIUM` y `HIGH`.
- Podra incluir notas opcionales.
- Cuando el coleccionista adquiera el elemento, el deseo se convertira en una copia de la coleccion.
- Durante la conversion se solicitaran los datos propios de la copia, como estado, ubicacion, fecha y precio.
- Tras una conversion correcta, el deseo sera eliminado.

### 4.10 Imagenes

- En el MVP se permitira una imagen principal por elemento del catalogo.
- La imagen representara el elemento general, por ejemplo la portada de un libro.
- Las fotografias privadas de copias concretas podran añadirse en una fase posterior.
- Se definiran limites de formato y tamaño antes de implementar la carga de archivos.

## 5. Reglas de negocio

1. Cada usuario esta vinculado a un unico perfil de coleccionista.
2. Una coleccion pertenece a un unico coleccionista.
3. Una copia pertenece a una unica coleccion y referencia un unico elemento del catalogo.
4. Una coleccion puede contener copias de diferentes tipos.
5. Se permiten varias copias del mismo elemento dentro de una o varias colecciones del propietario.
6. Los datos de una copia son privados, salvo cuando se muestran como parte de una coleccion publica.
7. Una coleccion publica solo puede modificarse por su propietario o por un administrador.
8. Cada coleccion dispone de una unica lista de deseos.
9. Un deseo pertenece a una unica coleccion.
10. La conversion de un deseo debe crear la copia y eliminar el deseo dentro de una operacion consistente.
11. Un elemento del catalogo puede tener cero o varios identificadores.
12. La combinacion de tipo y valor de un identificador debe evitar duplicados incompatibles en el catalogo.
13. Un elemento con referencias desde copias o deseos no puede eliminarse.
14. Todos los importes se expresan en euros.
15. Los precios no pueden ser negativos.
16. El valor actual y el precio de compra son independientes y opcionales.
17. El propietario decide y puede cambiar la visibilidad de cada coleccion.
18. No se permite la administracion compartida de colecciones.

## 6. Modelo conceptual

```text
User 1 --- 1 CollectorProfile
CollectorProfile 1 --- N Collection
CollectorProfile 1 --- N Location
Collection 1 --- N CollectionCopy
Collection 1 --- 1 Wishlist
Wishlist 1 --- N WishlistEntry
CollectionCopy N --- 1 CatalogItem
WishlistEntry N --- 1 CatalogItem
CatalogItem N --- 1 ItemType
CatalogItem 1 --- N ItemIdentifier
CatalogItem N --- N Category
CatalogItem N --- N Creator
CatalogItem 1 --- N AttributeValue
ItemType 1 --- N AttributeDefinition
CatalogItem 1 --- 0..1 MainImage
```

La separacion entre `CatalogItem` y `CollectionCopy` es esencial:

- `CatalogItem` contiene informacion compartida, como titulo, autor, año o ISBN.
- `CollectionCopy` contiene informacion privada de un ejemplar, como estado, ubicacion, fecha de adquisicion y precios.

## 7. Casos de uso principales

### CU-01: Gestionar una coleccion

El coleccionista crea, consulta, modifica o elimina una coleccion y selecciona su visibilidad.

### CU-02: Consultar una coleccion publica

Un visitante o usuario consulta una coleccion publica sin posibilidad de modificarla.

### CU-03: Registrar un elemento en el catalogo

El coleccionista selecciona un tipo, introduce los atributos correspondientes y añade identificadores opcionales.

### CU-04: Añadir una copia

El coleccionista selecciona un elemento del catalogo y registra su ejemplar en una de sus colecciones.

### CU-05: Gestionar una copia

El coleccionista consulta, modifica o elimina los datos de una copia de su propiedad.

### CU-06: Crear un tipo personalizado

El coleccionista crea un tipo y define sus atributos configurables.

### CU-07: Añadir un deseo

El coleccionista añade a la lista de una coleccion un elemento existente o uno nuevo, indicando su prioridad.

### CU-08: Marcar un deseo como adquirido

El coleccionista completa los datos de adquisicion. El sistema crea la copia y elimina el deseo.

### CU-09: Buscar y filtrar elementos

El usuario busca elementos por titulo, tipo, categoria o identificador y filtra las copias de sus colecciones.

## 8. Criterios de aceptacion generales del MVP

- Un usuario puede crear mas de una coleccion.
- Una coleccion admite copias de tipos diferentes.
- Dos usuarios pueden asociar copias privadas al mismo elemento del catalogo.
- Un usuario no puede modificar las colecciones ni las copias de otro usuario.
- Una coleccion privada no es visible para otros usuarios.
- Una coleccion publica se puede consultar, pero no modificar, por terceros.
- Se pueden crear elementos sin identificadores y con varios identificadores.
- Se pueden registrar varias copias del mismo elemento.
- Los formularios respetan los atributos definidos por el tipo de elemento.
- Cada coleccion mantiene su propia lista de deseos.
- La conversion de un deseo crea exactamente una copia y elimina el deseo correspondiente.
- Los importes se validan y se representan en euros.
- Un elemento referenciado no se puede eliminar del catalogo.

## 9. Arquitectura prevista

### 9.1 Microservicios

#### identity-service

- Usuarios.
- Perfiles de coleccionista.
- Roles.
- Autenticacion y JWT en una fase posterior.

#### catalog-service

- Catalogo comun.
- Tipos de elemento.
- Definiciones y valores de atributos.
- Identificadores.
- Categorias y creadores.
- Imagen principal.

#### collection-service

- Colecciones.
- Copias.
- Ubicaciones.
- Listas de deseos.
- Conversion de deseos en copias.

#### api-gateway

- Se incorporara en una fase posterior.
- Sera el punto de entrada para el frontend.
- Aplicara autenticacion y enrutamiento centralizados.

### 9.2 Comunicacion

- La comunicacion inicial entre servicios sera REST sincrona.
- `collection-service` consultara `catalog-service` para validar elementos del catalogo.
- Se configuraran timeouts y respuestas de error controladas.
- No se utilizaran Kafka, RabbitMQ ni circuit breakers en el MVP inicial.

## 10. Requisitos tecnicos

- Java 17.
- Spring Boot 3.x.
- Maven.
- API REST con JSON.
- Persistencia mediante Spring Data JPA.
- H2 durante la primera fase.
- Flyway desde el inicio para versionar el esquema.
- PostgreSQL en una fase posterior.
- OpenAPI/Swagger para documentar y probar los endpoints.
- Angular 17 para el frontend posterior.
- Monorepositorio.
- Una base de datos independiente por microservicio.

## 11. Persistencia y migracion

- Durante el desarrollo inicial se utilizara H2.
- El perfil de pruebas utilizara H2 en memoria.
- El perfil de desarrollo podra utilizar H2 persistida en archivo.
- Las migraciones se gestionaran con Flyway.
- Se evitaran inicialmente funciones exclusivas de H2 o PostgreSQL.
- Los atributos configurables se modelaran con tablas relacionales, no con `JSONB`.
- La migracion a PostgreSQL se realizara en una fase posterior.
- Tras la migracion se añadiran pruebas de integracion con PostgreSQL mediante Testcontainers.

## 12. Pruebas

Las pruebas se incorporaran desde el inicio y creceran con cada fase.

### 12.1 Pruebas unitarias

- Servicios de dominio.
- Validaciones.
- Reglas de permisos.
- Conversion de deseos en copias.
- Reglas de eliminacion e integridad.

### 12.2 Pruebas de integracion

- Repositorios JPA con H2.
- Controladores REST.
- Migraciones Flyway.
- Comunicacion entre servicios en los flujos principales.

### 12.3 Pruebas posteriores

- Testcontainers con PostgreSQL.
- Pruebas de seguridad con JWT.
- Pruebas de integracion del API Gateway.
- Pruebas extremo a extremo con Angular.

## 13. Requisitos no funcionales

- Validacion de todas las entradas de la API.
- Respuestas de error consistentes y comprensibles.
- Paginacion en listados de catalogo, colecciones, copias y deseos.
- Busqueda por titulo, tipo, categoria e identificador.
- Separacion estricta de datos privados entre coleccionistas.
- Documentacion OpenAPI actualizada.
- Migraciones de base de datos reproducibles.
- Codigo organizado por responsabilidades y servicios.
- Interfaz web adaptable a escritorio y dispositivos moviles.
- Almacenamiento seguro de contraseñas cuando se incorpore autenticacion.

## 14. Fuera de alcance

- Prestamos de elementos.
- Historial de precios o valoraciones.
- Varias monedas o conversion de divisas.
- Importacion desde CSV.
- Integraciones automaticas con ISBN, IMDb, EAN u otras API externas.
- Etiquetas libres.
- Estadisticas e informes.
- Comentarios, seguidores y otras funciones sociales.
- Auditoria completa de cambios.
- Copias de seguridad y exportacion desde la aplicacion.
- Notificaciones de elementos deseados.
- Colaboracion entre usuarios para administrar una coleccion.

## 15. Fases propuestas

### Fase 1: Catalogo

- Estructura inicial del monorepositorio.
- `catalog-service` con H2 y Flyway.
- Tipos, atributos, elementos e identificadores.
- Plantillas iniciales para libros y peliculas.
- API REST, documentacion OpenAPI y pruebas.

### Fase 2: Colecciones

- `collection-service` con su propia H2.
- Colecciones, copias, ubicaciones y visibilidad.
- Comunicacion REST con el catalogo.
- Pruebas unitarias y de integracion.

### Fase 3: Lista de deseos

- Lista independiente por coleccion.
- Prioridades.
- Conversion transaccional de deseos en copias.

### Fase 4: Identidad y seguridad

- `identity-service`.
- Registro e inicio de sesion.
- Roles `USER` y `ADMIN`.
- JWT.
- API Gateway.

### Fase 5: PostgreSQL

- Perfiles de configuracion para PostgreSQL.
- Adaptacion y validacion de migraciones Flyway.
- Testcontainers.
- Docker Compose para servicios y bases de datos.

### Fase 6: Frontend

- Aplicacion Angular 17.
- Autenticacion.
- CRUD de colecciones, catalogo y copias.
- Formularios dinamicos segun el tipo de elemento.
- Lista de deseos.
- Consulta publica de colecciones.
- Diseño adaptable.

## 16. Decisiones pendientes de detalle

Estas decisiones no bloquean el inicio, pero deberan concretarse antes de implementar la funcionalidad correspondiente:

- Version menor exacta de Spring Boot 3.x.
- Limites de tamaño y formatos permitidos para imagenes.
- Estrategia de almacenamiento de imagenes: sistema de archivos, base de datos o almacenamiento de objetos.
- Campos exactos del perfil del coleccionista.
- Criterio exacto para detectar duplicados en el catalogo.
- Comportamiento de borrado de una coleccion con copias o deseos.
- Nivel de detalle que una coleccion publica muestra sobre precios, ubicaciones y notas privadas.
- Reglas para editar un elemento compartido que ya utilizan otros coleccionistas.
- Politica de nombres duplicados para tipos, colecciones, categorias y ubicaciones.
