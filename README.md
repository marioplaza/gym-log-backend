# GymLog API

GymLog API es el backend para una aplicación de seguimiento de entrenamientos de gimnasio. Está construido con Java 21, Spring Boot 3 (WebFlux) y sigue un enfoque reactivo y moderno con **arquitectura multi-tenant**.

## Características Principales

- **Stack Moderno**: Java 21, Spring WebFlux, R2DBC.
- **API-First**: La API se define y genera a partir de una especificación OpenAPI.
- **Multi-Tenant**: Arquitectura por esquemas de BD que soporta múltiples clientes/gimnasios.
- **Seguridad**: Autenticación híbrida con tokens sociales y JWT propio con contexto de tenant.
- **Base de Datos**: PostgreSQL con migraciones gestionadas por Liquibase multi-tenant.
- **Calidad de Código**: Lógica de negocio robusta, manejo de errores centralizado y logging estructurado por perfiles.
- **Containerizado**: Listo para desplegar con Docker.

---

## 1. Prerrequisitos

Para construir y ejecutar este proyecto localmente, necesitarás:

- **Java 21**: Asegúrate de tener el JDK 21 instalado.
- **Maven 3.8+**: Para la gestión de dependencias y la construcción del proyecto.
- **Docker y Docker Compose**: Para ejecutar la aplicación en un entorno containerizado.
- **Una instancia de PostgreSQL** (Opcional, solo para desarrollo sin Docker).

---

## 2. Configuración para Desarrollo Local

### 2.1. Perfiles de Spring

La aplicación utiliza perfiles de Spring para gestionar diferentes configuraciones:
- **`dev`**: Perfil por defecto para desarrollo local. Utiliza logs de texto plano en la consola para facilitar la lectura.
- **`prod`**: Perfil para producción. Utiliza logs estructurados en formato JSON, ideales para sistemas de monitorización.

El perfil por defecto es `dev` y está configurado en `src/main/resources/application.yml`.

### 2.2. Configuración de la Base de Datos

Si no usas Docker, asegúrate de tener una base de datos PostgreSQL en ejecución. Los detalles de la conexión se configuran en `src/main/resources/application.yml`:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/gymlog
    username: user
    password: password
```

### 2.3. Configuración de Autenticación JWT y Proveedores Sociales

El proyecto utiliza un sistema híbrido de autenticación con **validación real** de tokens sociales:

1. **Login Social**: Los clientes se autentican con proveedores sociales (Google, Apple, Facebook) y envían el token al endpoint `/api/v1/auth/social-login`.
2. **Validación Real**: El backend valida el token contra el proveedor correspondiente usando sus APIs oficiales.
3. **JWT Propio**: Después de la validación exitosa, crea/actualiza el usuario y devuelve un JWT propio.
4. **Autorización**: Las siguientes peticiones usan el JWT de la aplicación.

La configuración se encuentra en `src/main/resources/application.yml`:

```yaml
gymlog:
  security:
    jwt-key: "una-clave-secreta-muy-larga-y-segura-para-firmar-tokens-jwt-en-desarrollo"
    social:
      google:
        client-ids:
          - "tu-google-client-id.apps.googleusercontent.com"
      facebook:
        app-id: "tu-facebook-app-id"
        app-secret: "tu-facebook-app-secret"
      apple:
        team-id: "tu-apple-team-id"
        bundle-id: "com.tu-app.bundle-id"
```

**⚠️ Importante**: 
- En producción, usa claves más seguras y almacénalas como variables de entorno
- Configura los client IDs/secrets reales de tus aplicaciones en cada proveedor
- Para Google: Obtén el client ID desde Google Cloud Console
- Para Facebook: Obtén app ID/secret desde Facebook Developers
- Para Apple: Configura team ID y bundle ID desde Apple Developer

### 2.4. Arquitectura Multi-Tenant por Esquemas

La aplicación implementa una arquitectura **multi-tenant por esquemas** que permite servir múltiples clientes (gimnasios) desde una sola instancia:

#### **Estructura de Base de Datos**

- **Esquema `public`**: Contiene únicamente la tabla `tenants` con información de cada cliente/gimnasio
- **Esquemas de tenant**: Cada cliente tiene su propio esquema (ej: `gym_001`, `gym_002`) con todas las tablas de la aplicación

#### **Gestión de Migraciones**

- **Automática**: Al arrancar la aplicación, `TenantLiquibaseManager` ejecuta migraciones en todos los esquemas de tenant
- **Iterativa**: Las migraciones se aplican esquema por esquema, excluyendo el `public`
- **Resiliente**: Si falla una migración en un esquema, continúa con los demás

#### **Resolución de Tenant**

- **JWT con Tenant**: Cada token incluye un `tenantId` que identifica el cliente
- **Contexto Automático**: Cada petición se ejecuta automáticamente en el esquema correcto
- **Aislamiento**: Los datos están completamente aislados entre clientes

#### **Creación de Nuevos Clientes**

```bash
# Crear un nuevo gimnasio/cliente
POST /api/v1/tenants
{
  "name": "Gimnasio Central",
  "email": "admin@gimnasiocentral.com",
  "plan": "premium"
}
```

Al crear un cliente:
1. Se genera un registro en la tabla `tenants` (esquema `public`)
2. Se crea automáticamente su esquema dedicado (ej: `gym_003`)
3. Se ejecutan todas las migraciones de Liquibase en el nuevo esquema
4. El cliente queda listo para usar inmediatamente

---

## 3. Construcción y Ejecución

### 3.1. Ejecutar en Modo de Desarrollo

Puedes lanzar la aplicación directamente con el plugin de Maven para Spring Boot. Con el perfil `dev` activo por defecto, verás logs de texto en la consola.

```bash
mvn spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

### 3.2. Construir el Artefacto (JAR)

Para compilar el proyecto y empaquetarlo en un fichero JAR ejecutable, ejecuta:

```bash
mvn clean install
```

El JAR resultante se encontrará en el directorio `target/`.

---

## 4. Ejecutar con Docker (Recomendado)

La forma más sencilla de levantar todo el entorno es usando Docker Compose. El `docker-compose.yml` está configurado para activar automáticamente el perfil `prod`.

### 4.1. Construir y Levantar los Contenedores

Desde la raíz del proyecto, ejecuta el siguiente comando:

```bash
docker-compose up --build
```

- `--build`: Fuerza la reconstrucción de la imagen de la API si has hecho cambios en el código.

Este comando levantará la API (con logs en JSON) y la base de datos. La API estará accesible en `http://localhost:8080`.

### 4.2. Detener los Contenedores

Para detener los servicios, presiona `Ctrl+C`. Para eliminarlos por completo, ejecuta:

```bash
docker-compose down
```

---

## 5. Documentación y Endpoints

### 5.1. Documentación de la API (Swagger UI)

Una vez que la aplicación está en ejecución, puedes acceder a la documentación interactiva de la API (Swagger UI) en tu navegador:

[**http://localhost:8080/swagger-ui.html**](http://localhost:8080/swagger-ui.html)

### 5.2. Endpoints de Autenticación

- **Login Social (Público)**: `POST /api/v1/auth/social-login`
  - Acepta tokens de Google, Apple o Facebook
  - Devuelve un JWT de la aplicación (con `tenantId`) y la información del usuario
- **Usuario Actual (Autenticado)**: `GET /api/v1/auth/me`
  - Requiere JWT en el header `Authorization: Bearer <token>`
  - Devuelve la información del usuario autenticado

### 5.3. Endpoints de Gestión de Tenants

- **Crear Cliente/Gimnasio**: `POST /api/v1/tenants`
  - Crea un nuevo cliente con su propio esquema de BD
  - Ejecuta automáticamente las migraciones de Liquibase
  - Requiere permisos especiales (super admin)
- **Listar Clientes**: `GET /api/v1/tenants`
  - Lista todos los clientes/gimnasios registrados
  - Solo accesible para super admins
- **Obtener Cliente**: `GET /api/v1/tenants/{id}`
  - Obtiene información de un cliente específico

### 5.4. Endpoints de Actuator

- **Health Check (Público)**: `GET /management/health`
- **Endpoints de Admin (Requieren rol `ADMIN`)**:
  - `GET /management/prometheus`
  - `GET /management/loggers`
  - `GET /management/info`
