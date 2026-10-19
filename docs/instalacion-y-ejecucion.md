# Instalación y ejecución

## 1. Software necesario

- **Java JDK 17** para compilar y ejecutar los servicios Spring Boot.
- **Maven 3.9 o posterior**. El repositorio no incluye Maven Wrapper.
- **Node.js 26** y **npm 11** para Angular 22.
- **STS/Eclipse** recomendado para ejecutar los servicios; navegador moderno para usar la interfaz.

H2 y Flyway se incluyen en los servicios: no hay que instalarlos aparte. Comprueba las versiones con `java -version`, `mvn -version`, `node --version` y `npm --version`.

## 2. Preparar el entorno

Los tres servicios requieren el mismo secreto JWT, de al menos 32 caracteres. Para una demo local puedes usar un valor propio, solo de desarrollo; no reutilices secretos de producción.

En STS, configura `ACERVO_JWT_SECRET` en la pestaña **Environment** de las configuraciones de ejecución de `identity-service`, `catalog-service` y `collection-service`. Asigna exactamente el mismo valor en las tres. En `collection-service`, `ACERVO_CATALOG_URL` es opcional y por defecto vale `http://localhost:8081`.

El perfil predeterminado es `dev`; carga datos de ejemplo del catálogo y aplica las migraciones Flyway al iniciar.

## 3. Iniciar el backend

Importa el POM raíz `pom.xml` en STS como proyecto Maven. Ejecuta las clases principales en este orden y deja cada servicio corriendo:

1. `IdentityServiceApplication` — puerto `8082`.
2. `CatalogServiceApplication` — puerto `8081`.
3. `CollectionServiceApplication` — puerto `8083`.

También puedes iniciar cada servicio desde una terminal en la raíz del repositorio. Define la misma variable `ACERVO_JWT_SECRET` en cada terminal antes de ejecutar:

```powershell
$env:ACERVO_JWT_SECRET = 'cambia-este-secreto-local-por-uno-de-32-caracteres'
mvn -pl services/identity-service spring-boot:run
```

Repite el comando sustituyendo el módulo por `services/catalog-service` y `services/collection-service`. Para la última terminal puedes añadir:

```powershell
$env:ACERVO_CATALOG_URL = 'http://localhost:8081'
```

## 4. Iniciar el frontend

En otra terminal:

```powershell
cd frontend
npm ci
npm start
```

Abre `http://localhost:4200`. El proxy de Angular envía las peticiones a los puertos configurados; no hace falta configurar CORS.

## 5. Probar la aplicación

Regístrate desde la pantalla inicial e inicia sesión. Después puedes crear o editar fichas, crear tipos personales y atributos, crear una colección, añadir deseos y convertir un deseo en ejemplar. Las bases H2 locales conservan los datos entre ejecuciones.

Con el selector **Estilo**, visible en todas las pantallas, puedes cambiar entre las interfaces Clásico, Expedición y Neón. La elección se recuerda en el navegador.

Para validar el backend antes de la demo, ejecuta `clean test` desde el proyecto raíz en STS. No se necesita Bruno para arrancar la interfaz.