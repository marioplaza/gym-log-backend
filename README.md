# GymLog API

GymLog API es el backend para una aplicación de seguimiento de entrenamientos de gimnasio. Está construido con Java 21, Spring Boot 3 (WebFlux) y sigue un enfoque reactivo y moderno.

## Características Principales

- **Stack Moderno**: Java 21, Spring WebFlux, R2DBC.
- **API-First**: La API se define y genera a partir de una especificación OpenAPI.
- **Seguridad**: Autenticación delegada a proveedores sociales (OAuth2).
- **Base de Datos**: PostgreSQL con migraciones gestionadas por Liquibase.
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

### 2.3. Configuración de Autenticación (OAuth2)

El proyecto utiliza OAuth2 para la autenticación. Debes proporcionar tus propias credenciales de cliente en `src/main/resources/application.yml`. Reemplaza los valores de `YOUR_..._ID`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
          # Puedes añadir otros proveedores como Facebook o Apple aquí
```

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

### 5.2. Endpoints de Actuator

- **Health Check (Público)**: `GET /management/health`
- **Endpoints de Admin (Requieren rol `ADMIN`)**:
  - `GET /management/prometheus`
  - `GET /management/loggers`
  - `GET /management/info`
