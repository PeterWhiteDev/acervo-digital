# Hoja de ruta y seguimiento de Acervo Digital

## Estado actual

- Última versión publicada: `v0.6.1`.
- Rama principal: `master`.
- Stack actual: Java 17, Spring Boot 3.5.6, Maven, H2 y Flyway.
- Interfaz Angular 22 implementada; PostgreSQL queda para el hito `1.0.0`.
- La administración de cuentas `0.7.0` está integrada en `develop` mediante la PR #12; el trabajo actual es reconciliar documentación en `feature/documentacion-pendiente`.
- El usuario ejecuta Maven y las pruebas desde Eclipse STS. Los cambios de Git se gestionan desde SourceTree/STS.

La versión `0.x` indica que el MVP sigue en desarrollo. `1.0.0` se reservará para la primera versión completa, integrada y estable.

## Hitos

### `0.1.0` — Catálogo inicial: completado

- Tipos de elemento globales y personales.
- Definiciones de atributos tipados y valores dinámicos.
- Fichas compartidas del catálogo.
- Datos de demostración para largometrajes de Guy Ritchie y Quentin Tarantino, y clásicos de la literatura española.
- Migraciones Flyway, pruebas y publicación del release `v0.1.0`.

### `0.2.0` — Completar catálogo: completado

- Añadir varios identificadores opcionales por ficha, como ISBN-10, ISBN-13, EAN y códigos de edición.
- Definir normalización, validación y reglas de unicidad por esquema.
- Permitir consultar y modificar fichas con identificadores.

**Criterio de aceptación:** crear, consultar y actualizar un libro con más de un identificador; rechazar identificadores inválidos o duplicados según las reglas acordadas.

La implementación está publicada y validada con pruebas y Bruno. La unicidad implementada es global por combinación de esquema y valor normalizado. La protección de borrado se aplazó hasta disponer de referencias entre servicios y sigue pendiente; se incluye en el trabajo de integración de `1.0.0`.

### `0.3.0` — Usuarios y seguridad: completado

- Incorporar usuarios y perfiles reales de coleccionista.
- Añadir roles `USER` y `ADMIN` y autenticación/autorización con JWT.
- Sustituir `DevelopmentCollectorProvider` por el usuario autenticado.
- Restringir tipos personales a su propietario y permitir la administración de plantillas globales solo a administradores.

El flujo de registro/login, JWT HS256 y autorización del catálogo está publicado y validado con 20 pruebas y Bruno.

**Criterio de aceptación:** usuarios distintos no pueden consultar ni modificar recursos personales ajenos; los permisos administrativos se verifican mediante pruebas.

### `0.4.0` — Colecciones y ejemplares: incluido en `v0.6.0`

- Crear `collection-service`.
- Permitir varias colecciones por coleccionista y establecer visibilidad `PRIVATE` o `PUBLIC`.
- Añadir ejemplares privados que referencien fichas del catálogo.
- Guardar estado, ubicación, fecha de adquisición, precio de compra, valor actual en euros y notas.

El módulo `collection-service` ya tiene CRUD JWT de colecciones, ubicaciones y copias. Las copias validan fichas mediante consulta al catálogo con timeout, admiten ubicación propia y su vista pública expone únicamente título y estado. Flyway V3 elimina copias al borrar una colección y protege ubicaciones asignadas. El usuario confirmó `BUILD SUCCESS` con 38 pruebas (18 catálogo, 3 identidad y 17 colección) y que todas las pruebas manuales de Bruno fueron bien, incluidas edición, privacidad pública y reglas de borrado.

**Criterio de aceptación:** un usuario puede organizar varios ejemplares en colecciones distintas, incluso si comparten ficha de catálogo; la privacidad se respeta.

La protección de borrado de fichas del catálogo referenciadas por copias o deseos no forma parte de lo ya validado y sigue pendiente para la integración `1.0.0`.

### `0.5.0` — Lista de deseos: incluido en `v0.6.0`

- Añadir una lista de deseos por colección.
- Guardar prioridad y notas opcionales.
- Convertir un deseo adquirido en ejemplar mediante una operación transaccional.

El CRUD de deseos y la conversión transaccional están implementados. Se solicita condición y fecha de adquisición; ubicación y precios son opcionales, y las notas del deseo se conservan si la conversión no envía otras. Se incluyó en `v0.6.0`. El usuario validó manualmente en Bruno el CRUD de deseos y su conversión a ejemplar el 2026-10-20.

**Criterio de aceptación:** la conversión crea el ejemplar y elimina el deseo, o no aplica ninguno de los dos cambios si falla la operación.

### `0.6.0` — Interfaz Angular 22: publicada

- Crear el frontend Angular 22, compatible con Node.js 26 del entorno local.
- Consultar el catálogo y gestionar tipos, atributos, colecciones, ejemplares y deseos.
- Integrar autenticación y autorización con la API.

El frontend integra registro/login JWT, rutas protegidas, catálogo paginado, edición de fichas con atributos/identificadores, tipos personales, atributos, colecciones, copias, ubicaciones y deseos. El proxy local enruta las peticiones a los tres servicios. La versión Maven `0.6.0` está publicada. Backend `clean install` y build/tests del frontend pasaron. El usuario completó la validación manual de extremo a extremo de los flujos principales el 2026-10-20.

**Criterio de aceptación:** los flujos principales del MVP se pueden completar desde la interfaz usando la API, con validaciones y errores comprensibles.

### `0.6.1` — Temas de interfaz: publicada

- Selector fijo en todas las pantallas con tres estilos guardados en el navegador: Clásico, Expedición y Neón.
- Cada tema cambia colores, tipografías, logo, fondo y la ubicación de navegación, filtros y listas.
- Integrado en `develop` mediante la PR #10, fusionado en `master` mediante la PR #11 y publicado como GitHub Release `v0.6.1`. El usuario confirmó `clean install` en STS con `BUILD SUCCESS` en los cuatro módulos. El frontend compila, sus tres pruebas unitarias pasan y el usuario confirmó manualmente que el selector de temas funciona.

### `0.7.0` — Administración de usuarios: implementada y validada en `develop`

- Añadir navegación y pantalla **Usuarios** solo para `ADMIN`, después de Colecciones, Catálogo y Tipos/Atributos; la aplicación abre por defecto en Colecciones.
- Permitir administrar otras cuentas, promover/degradar roles, restablecer contraseña y desactivar/reactivar sin borrar sus datos.
- Mostrar datos de cuenta sin credenciales y cantidades de colecciones, ejemplares por colección y agrupación por ubicación, sin títulos, precios ni notas.
- Proteger identity-service y collection-service con validación JWT/rol; conservar al menos un administrador activo y bloquear operaciones administrativas sobre la propia cuenta.
- La interfaz muestra etiquetas en castellano para enums y maneja JWT caducados volviendo al login con un aviso claro; `503` indica servicio no disponible.
- Build Angular correcto y 9/9 pruebas frontend aprobadas. El usuario confirmó `clean install` en STS y smoke test con cuentas `USER` y `ADMIN`.
- PR #12 (`feature/admin-users` → `develop`) fusionada. La publicación de `0.7.0` queda pendiente.

### `1.0.0` — MVP completo y estable

- Integrar los servicios y documentar la API.
- Completar pruebas de permisos, integración y flujos principales.
- Implementar la protección contra borrado de fichas referenciadas por copias o deseos y resolver la coordinación entre servicios.
- Retomar los hallazgos técnicos aplazados en el seguimiento del 2026-10-14.
- Planificar categorías, creadores, imagen principal y búsqueda por título, tipo, categoría e identificador.
- Preparar la migración de H2 a PostgreSQL y verificarla con pruebas sobre PostgreSQL.
- Revisar configuración, documentación de instalación y publicación estable.

**Criterio de aceptación:** los flujos acordados funcionan de extremo a extremo, las pruebas pasan y el despliegue puede inicializarse desde cero con la documentación disponible.

## Forma de trabajo y versiones

- Cada funcionalidad se desarrolla en una rama `feature/*` creada desde `develop` y se integra mediante Pull Request a `develop`. Para publicar una versión, se integra `develop` en `master` mediante Pull Request.
- Antes de cerrar un hito, el usuario ejecuta Maven y las pruebas desde STS y valida los flujos manuales acordados.
- Al completar un hito, se actualiza la versión Maven, se crea el tag correspondiente (`v0.2.0`, `v0.3.0`, etc.) y se publica un GitHub Release.
- Los cambios de versión y etiquetas se hacen después de validar el hito, no en cada commit.

## Seguimiento diario

Cada día que se trabaje en el proyecto se crea un archivo nuevo en `docs` con el formato `YYYY-MM-DD.md`. No se sobrescriben las notas anteriores. Cada nota resume el estado, los avances, las decisiones, las validaciones y el siguiente paso de esa jornada.

- [Seguimiento del 2026-10-13](2026-10-13.md)
- [Seguimiento del 2026-10-14](2026-10-14.md)
- [Seguimiento del 2026-10-19](2026-10-19.md)
- [Seguimiento del 2026-10-20](2026-10-20.md)
- [Instalación y ejecución](instalacion-y-ejecucion.md)