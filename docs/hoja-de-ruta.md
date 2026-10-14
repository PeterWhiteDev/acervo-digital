# Hoja de ruta y seguimiento de Acervo Digital

## Estado actual

- Última versión publicada: `v0.1.0`.
- Rama principal: `master`.
- Stack actual: Java 17, Spring Boot 3.5.6, Maven, H2 y Flyway.
- Interfaz Angular y PostgreSQL quedan para hitos posteriores.
- El usuario ejecuta Maven y las pruebas desde Eclipse STS. Los cambios de Git se gestionan desde SourceTree/STS.

La versión `0.x` indica que el MVP sigue en desarrollo. `1.0.0` se reservará para la primera versión completa, integrada y estable.

## Hitos

### `0.1.0` — Catálogo inicial: completado

- Tipos de elemento globales y personales.
- Definiciones de atributos tipados y valores dinámicos.
- Fichas compartidas del catálogo.
- Datos de demostración para largometrajes de Guy Ritchie y Quentin Tarantino, y clásicos de la literatura española.
- Migraciones Flyway, pruebas y publicación del release `v0.1.0`.

### `0.2.0` — Completar catálogo: siguiente hito

- Añadir varios identificadores opcionales por ficha, como ISBN-10, ISBN-13, EAN y códigos de edición.
- Definir normalización, validación y reglas de unicidad por esquema.
- Permitir consultar y modificar fichas con identificadores.
- Proteger la eliminación de fichas cuando existan referencias desde colecciones o listas de deseos.

**Criterio de aceptación:** crear, consultar y actualizar un libro con más de un identificador; rechazar identificadores inválidos o duplicados según las reglas acordadas.

### `0.3.0` — Usuarios y seguridad

- Incorporar usuarios y perfiles reales de coleccionista.
- Añadir roles `USER` y `ADMIN` y autenticación/autorización con JWT.
- Sustituir `DevelopmentCollectorProvider` por el usuario autenticado.
- Restringir tipos personales a su propietario y permitir la administración de plantillas globales solo a administradores.

**Criterio de aceptación:** usuarios distintos no pueden consultar ni modificar recursos personales ajenos; los permisos administrativos se verifican mediante pruebas.

### `0.4.0` — Colecciones y ejemplares

- Crear `collection-service`.
- Permitir varias colecciones por coleccionista y establecer visibilidad `PRIVATE` o `PUBLIC`.
- Añadir ejemplares privados que referencien fichas del catálogo.
- Guardar estado, ubicación, fecha de adquisición, precio de compra, valor actual en euros y notas.

**Criterio de aceptación:** un usuario puede organizar varios ejemplares en colecciones distintas, incluso si comparten ficha de catálogo; la privacidad se respeta.

### `0.5.0` — Lista de deseos

- Añadir una lista de deseos por colección.
- Guardar prioridad y notas opcionales.
- Convertir un deseo adquirido en ejemplar mediante una operación transaccional.

**Criterio de aceptación:** la conversión crea el ejemplar y elimina el deseo, o no aplica ninguno de los dos cambios si falla la operación.

### `0.6.0` — Interfaz Angular

- Crear el frontend Angular 17.
- Consultar el catálogo y gestionar tipos, atributos, colecciones, ejemplares y deseos.
- Integrar autenticación y autorización con la API.

**Criterio de aceptación:** los flujos principales del MVP se pueden completar desde la interfaz usando la API, con validaciones y errores comprensibles.

### `1.0.0` — MVP completo y estable

- Integrar los servicios y documentar la API.
- Completar pruebas de permisos, integración y flujos principales.
- Preparar la migración de H2 a PostgreSQL y verificarla con pruebas sobre PostgreSQL.
- Revisar configuración, documentación de instalación y publicación estable.

**Criterio de aceptación:** los flujos acordados funcionan de extremo a extremo, las pruebas pasan y el despliegue puede inicializarse desde cero con la documentación disponible.

## Forma de trabajo y versiones

- Cada hito se desarrolla en una rama de trabajo y se integra mediante Pull Request a `master`.
- Antes de cerrar un hito, el usuario ejecuta Maven y las pruebas desde STS y valida los flujos manuales acordados.
- Al completar un hito, se actualiza la versión Maven, se crea el tag correspondiente (`v0.2.0`, `v0.3.0`, etc.) y se publica un GitHub Release.
- Los cambios de versión y etiquetas se hacen después de validar el hito, no en cada commit.

## Seguimiento diario

Cada día que se trabaje en el proyecto se crea un archivo nuevo en `docs` con el formato `YYYY-MM-DD.md`. No se sobrescriben las notas anteriores. Cada nota resume el estado, los avances, las decisiones, las validaciones y el siguiente paso de esa jornada.

- [Seguimiento del 2026-10-13](2026-10-13.md)
- [Seguimiento del 2026-10-14](2026-10-14.md)