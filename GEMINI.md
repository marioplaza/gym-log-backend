# GymLog: Guía de Desarrollo y Especificaciones

Este documento sirve como guía para el desarrollo del proyecto utilizando la CLI de Gemini, especificando las tecnologías, arquitectura y requerimientos.

## 1. Tecnologías y Arquitectura

### 1.1. Stack Tecnológico Principal
- **Lenguaje**: Java 21
- **Framework**: Spring Boot 3.5.4 (existe aunque no lo conozcas)
- **Gestor de Dependencias**: Maven
- **Base de Datos**: PostgreSQL

### 1.2. Arquitectura y Paradigmas
- **Programación Reactiva**: Se utilizará el stack reactivo de Spring (WebFlux) para construir una aplicación no bloqueante y eficiente.
- **Acceso a Datos**: Se utilizará **R2DBC** (Reactive Relational Database Connectivity) para el acceso a datos reactivo. Las consultas SQL se escribirán manualmente para tener control total, sin ORMs como JPA/Hibernate.
- **Aprovechamiento de Java 21**:
    - **Records**: Utilizar `records` para la definición de DTOs (Data Transfer Objects), haciéndolos inmutables y concisos.
    - **Hilos Virtuales (Virtual Threads)**: Evaluar y utilizar hilos virtuales del Proyecto Loom donde aporten beneficios de concurrencia y escalabilidad, especialmente en tareas de I/O que no sean cubiertas por el modelo reactivo.

### 1.3. Mapeo, Generación de Código y Gestión de BD
- No usar LOMBOK bajo ningún concepto
- **Mapeo de Objetos**: Se usará **MapStruct** para las conversiones entre entidades de la base de datos y los DTOs de la API, automatizando el boilerplate y asegurando consistencia.
- **Generación de API (API-First)**:
    - El plugin `openapi-generator-maven-plugin` será configurado para generar las interfaces de la API (controladores) y los modelos (DTOs) a partir de una especificación OpenAPI (e.g., `openapi.yml`).
    - Se incluirá la dependencia `springdoc-openapi` para servir una UI de Swagger interactiva basada en la especificación OpenAPI del proyecto.
- **Migraciones de Base de Datos**: Se utilizará **Liquibase** para gestionar el versionado del esquema de la base de datos. Los cambios se definirán en ficheros de changelog (XML, YAML o SQL).

### 1.4. Arquitectura Multi-Tenant por Esquema
- **Concepto**: La aplicación soportará múltiples clientes (gimnasios) mediante una arquitectura multi-tenant basada en **esquemas de base de datos separados**.
- **Estructura de Base de Datos**:
    - **Esquema `public`**: Contiene únicamente la tabla de clientes/gimnasios (`tenants`) con información básica de cada cliente.
    - **Esquemas de tenant**: Cada cliente tiene su propio esquema (ej: `gym_001`, `gym_002`) con todas las tablas de la aplicación (usuarios, rutinas, ejercicios, etc.).
- **Gestión de Migraciones con Liquibase**:
    - Se implementará un `TenantLiquibaseManager` que ejecuta las migraciones de forma iterativa sobre cada esquema de tenant.
    - Las migraciones se ejecutan automáticamente al arrancar la aplicación.
    - El esquema `public` se excluye de las migraciones automáticas (solo contiene la tabla de tenants).
- **Resolución de Tenant**:
    - Cada petición debe incluir el `tenantId` en el JWT para identificar el esquema correspondiente.
    - Se implementará un `TenantResolver` que extrae el tenant del token y configura el contexto de base de datos.
- **Creación de Nuevos Clientes**:
    - Endpoint `/api/v1/tenants` para crear nuevos gimnasios.
    - Al crear un cliente, se genera automáticamente su esquema y se ejecutan las migraciones iniciales.

### 1.5. Seguridad: Autenticación y Autorización
- **Autenticación**:
    - Se implementará un sistema de autenticación basado en proveedores sociales (Social Login).
    - Proveedores soportados: **Google, Apple, Facebook**.
    - El flujo será:
        1. El cliente (frontend) realiza el login con el proveedor social y obtiene un token (e.g., JWT, access token).
        2. El cliente envía este token al endpoint `/api/v1/auth/social-login` del backend.
        3. El backend validará el token contra el proveedor correspondiente y, si es válido, creará o actualizará el usuario en la base de datos.
        4. El backend generará su propio JWT con la información del usuario **incluyendo el `tenantId`** y lo devolverá al cliente.
        5. Para las siguientes peticiones, el cliente usará el JWT de la aplicación (no el token social) en las cabeceras de autorización.
    - **No existirá un login tradicional con email/contraseña.**
- **Autorización**:
    - La aplicación manejará roles de usuario definidos en la base de datos de cada tenant.
    - Se utilizará Spring Security para proteger los endpoints según el rol del usuario autenticado.
    - **Aislamiento por Tenant**: Cada petición solo puede acceder a los datos del esquema correspondiente al `tenantId` del JWT.

### 1.6. Configuración y Despliegue
- **Fichero de Configuración**: Se utilizará `application.yml` en lugar de `application.properties` y Spring Profiles para la gestión de entornos (dev, prod, etc.).
- **Despliegue**: La aplicación será containerizada con **Docker** para asegurar un entorno de despliegue consistente.

### 1.7. Calidad, Pruebas y Operación
- **Documentación de Código**:
    - Todo el código público (clases, métodos, records) deberá estar documentado utilizando **Javadocs**.
    - Los comentarios deben explicar el "porqué" y no el "qué". Deben aclarar la intención, los contratos de los métodos (parámetros, retornos, excepciones) y cualquier comportamiento no obvio.
- **Manejo de Errores**:
    - Se implementará un `ControllerAdvice` global (`@RestControllerAdvice`) para centralizar el manejo de excepciones.
    - Se definirá una estructura de respuesta de error JSON estándar para toda la API, que incluya un código de error único, un mensaje legible y, opcionalmente, detalles técnicos en entornos de no producción.
- **Logging**:
    - Se utilizará **Logback** (incluido en Spring Boot) con una configuración que produzca **logs estructurados en formato JSON**.
    - Los logs incluirán un identificador de correlación (Correlation ID) para poder trazar una petición a través de todo el sistema.
    - Se definirán niveles de log apropiados para cada evento (e.g., `INFO` para flujos de negocio, `WARN` para situaciones inesperadas pero controladas, `ERROR` para fallos).
- **Monitorización y Gestión en Caliente**:
    - Se utilizará **Spring Boot Actuator** para exponer endpoints de gestión.
    - **Métricas**: Se configurará para exponer métricas en formato compatible con **Prometheus** (`/actuator/prometheus`).
    - **Gestión**: Se habilitarán endpoints para comprobar la salud (`/actuator/health`) y cambiar niveles de log en caliente (`/actuator/loggers`), entre otros. El acceso a los endpoints sensibles será securizado.
- **Pruebas Unitarias**: De momento no se harán. En un futuro se utilizará el stack estándar de **JUnit 5** y **Mockito** para probar la lógica de negocio de forma aislada.
- **Pruebas de Integración**: De momento no se harán. En un futuro se empleará **Testcontainers** para levantar una instancia real de PostgreSQL en un contenedor de Docker durante la fase de pruebas. Esto permitirá validar la capa de acceso a datos y los endpoints de la API contra una base de datos fidedigna.

---

## 2. Modelo de Datos y API (v2)

Esta sección describe la arquitectura de datos y el flujo de la aplicación, centrada en la flexibilidad, la personalización del usuario y el seguimiento del progreso a lo largo del tiempo.

### 2.1. Lógica Central

1.  **Banco de Ejercicios**: Existe un banco global de ejercicios (públicos y predefinidos). Los usuarios pueden añadir sus propios ejercicios a este banco, marcándolos como públicos o privados.
2.  **Rutinas Personalizadas**: Cada usuario crea sus propias rutinas. Una rutina consta de varios "días de entrenamiento".
3.  **Configuración de Días**: Para cada día, el usuario añade ejercicios desde el banco. La configuración clave (series, repeticiones, peso) no se almacena en esta plantilla.
4.  **Registro Dinámico**: Cuando un usuario entrena, la app le muestra los valores (series, repeticiones, peso) del **último entrenamiento registrado** para ese ejercicio.
5.  **Seguimiento del Progreso**: El usuario ajusta los valores si es necesario y, al finalizar, la aplicación guarda un **único registro consolidado** del rendimiento de ese ejercicio para esa sesión. Este historial alimenta las gráficas de progreso.

### 2.2. Entidades de la Base de Datos

#### Tablas de Usuario y Autenticación
- **`users`**: `id` (UUID, PK), `provider_id` (VARCHAR), `provider` (VARCHAR), `email` (VARCHAR, UNIQUE), `display_name` (VARCHAR), `created_at`, `updated_at`.
- **`roles`**: `id` (SERIAL, PK), `name` (VARCHAR, UNIQUE).
- **`user_roles`**: `user_id` (UUID, FK), `role_id` (INT, FK).

#### Tabla de Ejercicios (Banco Global)
- **`exercises`**:
    - `id` (UUID, PK): ID del ejercicio.
    - `name` (VARCHAR): Nombre (ej: "Press de Banca").
    - `description` (TEXT): Instrucciones.
    - `video_url` (VARCHAR, NULLABLE).
    - `target_muscle_group` (VARCHAR): Grupo muscular principal.
    - `created_by_user_id` (UUID, FK a `users.id`, NULLABLE): `NULL` para ejercicios del sistema.
    - `is_public` (BOOLEAN): `true` si es visible para otros, `false` si es privado del creador.
    - `created_at`, `updated_at`.

#### Tablas de Rutinas del Usuario
- **`routines`**:
    - `id` (UUID, PK): ID de la rutina.
    - `user_id` (UUID, FK a `users.id`): Propietario.
    - `name` (VARCHAR): Nombre (ej: "Mi Rutina de 5 Días").
    - `is_active` (BOOLEAN): Para marcar la rutina actual.
    - `created_at`, `updated_at`.

- **`routine_days`**:
    - `id` (UUID, PK): ID del día.
    - `routine_id` (UUID, FK a `routines.id`): A qué rutina pertenece.
    - `name` (VARCHAR): Nombre (ej: "Día de Empuje").
    - `order_num` (INT): Para ordenar los días.
    - `created_at`, `updated_at`.

- **`routine_exercises`** (La plantilla de qué ejercicios hacer):
    - `id` (UUID, PK).
    - `routine_day_id` (UUID, FK a `routine_days.id`).
    - `exercise_id` (UUID, FK a `exercises.id`).
    - `order_num` (INT): Orden de los ejercicios en el día.
    - `is_active` (BOOLEAN): Para desactivar un ejercicio sin borrarlo.
    - `notes` (TEXT, NULLABLE): Notas del usuario para este ejercicio en esta rutina.
    - `created_at`, `updated_at`.

#### Tablas de Registro de Historial
- **`workout_sessions`** (El contenedor de un entrenamiento):
    - `id` (UUID, PK): ID de la sesión.
    - `user_id` (UUID, FK a `users.id`).
    - `routine_day_id` (UUID, FK a `routine_days.id`, NULLABLE): Para registrar entrenamientos espontáneos.
    - `start_time`, `end_time`.
    - `notes` (TEXT, NULLABLE): Notas generales de la sesión.

- **`exercise_logs`** (El historial de rendimiento):
    - `id` (UUID, PK).
    - `workout_session_id` (UUID, FK a `workout_sessions.id`).
    - `routine_exercise_id` (UUID, FK a `routine_exercises.id`).
    - `sets_completed` (INT): Número de series realizadas.
    - `reps_achieved` (VARCHAR): Repeticiones por serie (ej: "8,8,7").
    - `weight_kg` (DECIMAL): Peso utilizado.
    - `notes` (TEXT, NULLABLE): Comentarios específicos del rendimiento.
    - `recorded_at` (TIMESTAMP).

### 2.3. Endpoints de la API (Visión General)

#### Gestión del Banco de Ejercicios
- `GET /api/v1/exercises`: Buscar ejercicios en el banco (con filtros por nombre, grupo muscular, etc.).
- `POST /api/v1/exercises`: Crear un nuevo ejercicio (público o privado).
- `PUT /api/v1/exercises/{id}`: Actualizar un ejercicio que hayas creado.

#### Gestión de Rutinas
- `GET /api/v1/routines`: Obtener todas las rutinas del usuario.
- `POST /api/v1/routines`: Crear una nueva rutina.
- `PUT /api/v1/routines/{id}`: Actualizar una rutina (ej: cambiar nombre, activarla).
- `DELETE /api/v1/routines/{id}`: Eliminar una rutina.
- `GET /api/v1/routines/{id}`: Obtener el detalle de una rutina con sus días y ejercicios.
- `POST /api/v1/routines/{id}/days`: Añadir un nuevo día a la rutina.
- `POST /api/v1/routine-days/{dayId}/exercises`: Añadir un ejercicio del banco a un día de la rutina.
- `DELETE /api/v1/routine-exercises/{routineExerciseId}`: Quitar un ejercicio de un día.
- `PATCH /api/v1/routine-exercises/{routineExerciseId}`: Actualizar un ejercicio en la rutina (ej: desactivarlo, cambiar notas).

#### Registro de Entrenamientos
- `POST /api/v1/workout-sessions`: Iniciar una nueva sesión de entrenamiento.
- `POST /api/v1/workout-sessions/{sessionId}/logs`: Registrar el rendimiento de un ejercicio en la sesión actual.
- `GET /api/v1/workout-sessions`: Ver el historial de sesiones.
- `GET /api/v1/workout-sessions/{sessionId}`: Ver el detalle de una sesión con todos sus `exercise_logs`.
- `GET /api/v1/routine-exercises/{routineExerciseId}/history`: Obtener el historial de `exercise_logs` para un ejercicio específico de una rutina, para pintar las gráficas de progreso.

### 2.4. Reglas de Autorización y Lógica

##### 2.4.1. Lógica de Autorización (Seguridad a Nivel de Recurso)

El principio fundamental es que **un usuario solo puede operar sobre sus propios datos**. Esto se debe implementar de forma rigurosa en la capa de servicio para cada endpoint que acceda o modifique recursos propiedad del usuario.

- **Validación de Propiedad Directa:**
    - Para recursos que tienen una columna `user_id` directa (como `routines` o `workout_sessions`), la lógica de validación es:
        1.  Obtener el `user_id` del usuario autenticado a través del token de seguridad.
        2.  Antes de ejecutar cualquier operación (lectura, modificación, borrado), recuperar el recurso de la base de datos por su ID.
        3.  Comparar el `user_id` del token con el `user_id` del recurso.
        4.  Si no coinciden, la operación debe fallar inmediatamente con un código de estado `403 Forbidden`.

- **Validación de Propiedad Indirecta:**
    - Para recursos anidados (como `routine_exercises` o `exercise_logs`), la validación debe "subir" por la jerarquía de relaciones hasta encontrar el `user_id`.
    - **Ejemplo**: Al modificar un `exercise_log` (`PATCH /api/v1/exercise-logs/{logId}`):
        1.  Se obtiene el `user_id` del token.
        2.  Se recupera el `exercise_log` por su `logId`.
        3.  A través de las claves foráneas, se navega `exercise_log` -> `workout_session`.
        4.  Se comprueba que el `user_id` en la `workout_session` coincida con el del token. Si no, se devuelve `403 Forbidden`.

- **Caso Especial: Banco de Ejercicios (`exercises`)**
    - **Lectura (`GET /api/v1/exercises`):** Una búsqueda debe devolver una combinación de:
        1.  Todos los ejercicios del sistema (`created_by_user_id IS NULL`).
        2.  Todos los ejercicios de otros usuarios marcados como públicos (`is_public = true`).
        3.  Todos los ejercicios creados por el usuario actual, tanto públicos como privados.
    - **Modificación/Borrado (`PUT`/`DELETE`):** Un usuario **solo** puede modificar o borrar un ejercicio si el `created_by_user_id` del ejercicio coincide con su `user_id`. Intentar modificar un ejercicio del sistema o de otro usuario debe resultar en un `403 Forbidden`.

##### 2.4.2. Lógica de Negocio (Reordenación Automática)

Esta lógica se aplica a las entidades que tienen una columna `order_num` para mantener una secuencia ordenada, como `routine_days` (dentro de una rutina) y `routine_exercises` (dentro de un día). El objetivo es evitar conflictos y mantener la integridad del orden cuando un elemento es insertado o su posición es actualizada.

Toda la lógica de reordenación debe ejecutarse dentro de una **transacción de base de datos** para garantizar la consistencia.

- **Al Crear un Nuevo Elemento (`POST`):**
    - El usuario envía un nuevo elemento con un `order_num` específico (ej: `order_num = 3`).
    - **Lógica del Backend:**
        1.  Buscar todos los elementos en el mismo grupo (ej: en el mismo `routine_day_id`) que tengan un `order_num` mayor o igual al del nuevo elemento (`>= 3`).
        2.  Para cada uno de esos elementos, incrementar su `order_num` en 1.
        3.  Insertar el nuevo elemento con el `order_num` solicitado (`3`).

- **Al Actualizar la Posición de un Elemento (`PUT`/`PATCH`):**
    - El usuario cambia el `order_num` de un elemento existente. Hay dos escenarios:
    - **1. Mover hacia arriba (ej: de la posición 5 a la 2):**
        1.  Identificar la posición antigua (`old = 5`) y la nueva (`new = 2`).
        2.  Buscar los elementos cuyas posiciones estén entre la nueva y la antigua (`order_num >= 2` y `order_num < 5`).
        3.  Incrementar el `order_num` de estos elementos en 1.
        4.  Actualizar el `order_num` del elemento movido a la nueva posición (`2`).
    - **2. Mover hacia abajo (ej: de la posición 2 a la 5):**
        1.  Identificar la posición antigua (`old = 2`) y la nueva (`new = 5`).
        2.  Buscar los elementos cuyas posiciones estén entre la antigua y la nueva (`order_num > 2` y `order_num <= 5`).
        3.  Decrementar el `order_num` de estos elementos en 1.
        4.  Actualizar el `order_num` del elemento movido a la nueva posición (`5`).

---

## 3. Plan de Desarrollo Secuencial

Este plan está dividido en fases, desde la configuración inicial hasta la implementación de las funcionalidades principales y los aspectos transversales.

### **Fase 0: Cimientos del Proyecto y Configuración**

El objetivo de esta fase es establecer la estructura del proyecto, la base de datos y la generación de código a partir de la API, dejando todo listo para empezar a implementar la lógica de negocio.

1.  **Inicialización del Proyecto Spring Boot:**
    *   Crear un nuevo proyecto Maven.
    *   Configurar el `pom.xml` con Java 21, Spring Boot 3.5.4 (WebFlux), y las dependencias iniciales: `spring-boot-starter-webflux`, `spring-boot-starter-r2dbc`, `r2dbc-postgresql`, y `spring-boot-devtools`.

2.  **Definición del Esquema de Base de Datos con Liquibase:**
    *   Añadir la dependencia de `liquibase-core` al `pom.xml`.
    *   Crear el primer fichero de changelog de Liquibase (e.g., `src/main/resources/db/changelog/db.changelog-master.xml`).
    *   Traducir **todas** las entidades del `GEMINI.md` (`users`, `roles`, `exercises`, `routines`, etc.) a sentencias `CREATE TABLE` en un nuevo changelog SQL o XML.

3.  **Definición de la API con OpenAPI:**
    *   Crear el fichero de especificación `openapi.yml` en la raíz del proyecto.
    *   Definir en el YAML los endpoints principales descritos en el `GEMINI.md`, empezando por la gestión de ejercicios (`/api/v1/exercises`). Incluir los DTOs (schemas) correspondientes usando `records` como guía.

4.  **Configuración de la Generación de Código y Conexión:**
    *   Configurar el `openapi-generator-maven-plugin` en el `pom.xml` para que lea el `openapi.yml` y genere las interfaces de los controladores y los DTOs en un paquete específico (e.g., `es.gymlog.api`).
    *   Configurar el `application.yml` con los perfiles `dev` y `prod`, y añadir la configuración de conexión a la base de datos PostgreSQL usando R2DBC.
    *   Añadir la dependencia `springdoc-openapi-starter-webflux-ui` para la UI de Swagger.

### **Fase 1: Implementación del Módulo de Ejercicios**

Nos centraremos en la primera entidad principal, que es el banco de ejercicios. Esto nos permitirá validar el flujo completo (API -> Controller -> Service -> Repository -> DB).

5.  **Desarrollo de la Capa de Datos y Lógica de Ejercicios:**
    *   Ejecutar el `mvn clean install` para generar las interfaces y DTOs desde el `openapi.yml`.
    *   Crear el `record` de la entidad `Exercise`.
    *   Crear la interfaz `ExerciseRepository` con los métodos para las consultas SQL manuales (CRUD y búsqueda con filtros).
    *   Configurar MapStruct y crear la interfaz `ExerciseMapper` para convertir entre la entidad `Exercise` y su DTO.
    *   Implementar `ExerciseService` con la lógica de negocio, incluyendo las reglas de autorización para ver ejercicios públicos/privados y modificar solo los propios.
    *   Crear `ExerciseController` que implemente la interfaz generada por OpenAPI y utilice el `ExerciseService`.

### **Fase 2: Gestión de Usuarios, Rutinas y Autorización**

Con el primer módulo funcionando, introducimos la gestión de usuarios y la seguridad, que es fundamental para el resto de la aplicación.

6.  **Implementación de Usuarios y Seguridad con Spring Security:**
    *   Implementar las entidades de usuario (`User`, `Role`, `UserRole`).
    *   Configurar Spring Security para la autenticación vía Social Login (OAuth2/OIDC), validando los tokens de los proveedores.
    *   Establecer la lógica para crear un usuario en la BD la primera vez que se autentica.

7.  **Desarrollo del Módulo de Rutinas:**
    *   Ampliar el `openapi.yml` con todos los endpoints de rutinas (`/api/v1/routines`, `/api/v1/routine-days`, etc.).
    *   Seguir el mismo patrón que con los ejercicios: generar API, crear entidades (`Routine`, `RoutineDay`, `RoutineExercise`), repositorios, mappers y servicios.
    *   Implementar la **lógica de autorización** en la capa de servicio para que un usuario solo pueda gestionar sus propias rutinas.
    *   Implementar la **lógica de reordenación automática** para `routine_days` y `routine_exercises` dentro de sus respectivos servicios.

### **Fase 3: Funcionalidad Principal - Registro de Entrenamientos**

Esta es la última parte del flujo de negocio principal, donde el usuario registra su progreso.

8.  **Desarrollo del Módulo de Registro de Sesiones:**
    *   Definir los endpoints restantes en `openapi.yml` (`/api/v1/workout-sessions`, `/api/v1/exercise-logs`, etc.).
    *   Implementar el flujo completo: entidades (`WorkoutSession`, `ExerciseLog`), repositorios, mappers, servicios y controladores.
    *   Asegurar que la lógica de autorización valida que los logs de ejercicios se asocien a sesiones del usuario autenticado.

### **Fase 4: Aspectos Transversales y Finalización**

Con la lógica de negocio completa, nos centramos en la calidad, operación y despliegue.

9.  **Implementación de Manejo de Errores y Logging:**
    *   Crear un `@RestControllerAdvice` global para centralizar el manejo de excepciones y devolver una estructura de error JSON consistente.
    *   Configurar Logback (`logback-spring.xml`) para producir logs estructurados en JSON, incluyendo un Correlation ID.

10. **Configuración de Actuator y Monitorización:**
    *   Añadir la dependencia de `spring-boot-starter-actuator`.
    *   Configurar `application.yml` para exponer los endpoints de `health`, `prometheus` y `loggers`.
    *   Añadir seguridad a los endpoints sensibles de Actuator.

11. **Documentación y Calidad de Código:**
    *   Realizar una pasada completa por el código para añadir Javadocs a todas las clases, records y métodos públicos, explicando el "porqué" de la lógica compleja.

12. **Implementación Multi-Tenant:**
    *   Implementar `TenantLiquibaseManager` para gestión de migraciones por esquema.
    *   Crear tabla de tenants en esquema `public`.
    *   Implementar `TenantResolver` para resolución automática de tenant por JWT.
    *   Configurar DataSource dinámico por tenant.
    *   Implementar endpoint `/api/v1/tenants` para creación de nuevos clientes/gimnasios.
    *   Modificar JWT para incluir `tenantId` en todos los tokens.

13. **Containerización con Docker:**
    *   Crear un `Dockerfile` para empaquetar la aplicación Spring Boot en una imagen de contenedor.
    *   Crear un `docker-compose.yml` que levante dos servicios: la aplicación y la base de datos PostgreSQL, conectándolos entre sí.

---

## 5. Flujos Multi-Tenant Detallados

### 5.1. Flujo de Creación de Nuevo Cliente/Gimnasio

1. **Petición**: `POST /api/v1/tenants` con datos del gimnasio
2. **Validación**: Verificar que el usuario tiene permisos para crear tenants
3. **Creación en BD**: 
   - Insertar registro en tabla `tenants` (esquema `public`)
   - Generar `schema_name` único (ej: `gym_001`)
4. **Creación de Esquema**: Ejecutar `CREATE SCHEMA gym_001`
5. **Migraciones Iniciales**: Ejecutar todos los changesets de Liquibase en el nuevo esquema
6. **Respuesta**: Devolver información del tenant creado

### 5.2. Flujo de Resolución de Tenant por Petición

1. **Extracción del JWT**: Obtener token de la cabecera `Authorization`
2. **Decodificación**: Extraer `tenantId` del claim del JWT
3. **Resolución de Esquema**: Consultar tabla `tenants` para obtener `schema_name`
4. **Configuración de Contexto**: Establecer esquema activo para la conexión R2DBC
5. **Ejecución**: Procesar la petición en el contexto del tenant correspondiente

### 5.3. Flujo de Migraciones Multi-Tenant

1. **Arranque de Aplicación**: `TenantLiquibaseManager` se ejecuta automáticamente
2. **Obtención de Tenants**: Consultar tabla `tenants` en esquema `public`
3. **Iteración por Esquema**: Para cada tenant activo:
   - Configurar Liquibase con `defaultSchema = tenant.schemaName`
   - Ejecutar migraciones pendientes
   - Registrar resultado en logs
4. **Manejo de Errores**: Si falla una migración, continuar con el siguiente tenant pero registrar el error