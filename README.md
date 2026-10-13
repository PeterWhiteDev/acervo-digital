# Acervo Digital

Monorepositorio para una aplicacion de colecciones personales. El nombre es provisional.

## Estructura

- `services/catalog-service`: primera API Spring Boot para el catalogo comun.
- `frontend`: reservado para la interfaz Angular.
- `docs`: analisis de requisitos y diseño tecnico.

El proyecto usa Java 17, Spring Boot 3.5.6, Maven, H2 y Flyway. La base H2 local se guarda en el directorio de trabajo desde el que se inicia el servicio.

## Tipos de elemento

La primera migracion crea las plantillas globales `Libro` y `Pelicula`. La API permite crear tipos personales y consultar los tipos globales y personales del coleccionista de desarrollo.

- `GET /api/v1/item-types`
- `GET /api/v1/item-types/{id}`
- `POST /api/v1/item-types`

Mientras no haya autenticacion, el coleccionista actual usa el UUID de desarrollo configurado en `application.yml`. Este proveedor se sustituira al integrar identidad/JWT.

## Comandos

Desde la raiz del proyecto:

```powershell
mvn test
mvn -pl services/catalog-service spring-boot:run
```

La API se iniciara en `http://localhost:8081`. La consola H2 estara disponible en `/h2-console` durante el desarrollo.