package es.gymlog.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Configurador de contexto de base de datos dinámico para arquitectura multi-tenant.
 * 
 * En R2DBC reactivo, no podemos cambiar el esquema de la conexión de forma global
 * como en JDBC tradicional. En su lugar, debemos ejecutar "SET search_path" 
 * al inicio de cada operación que requiera contexto de tenant.
 * 
 * Esta clase proporciona utilidades para:
 * 1. Configurar el search_path de PostgreSQL por operación
 * 2. Ejecutar operaciones en el contexto de un tenant
 * 3. Gestionar el contexto de esquema para operaciones de base de datos
 */
@Component
public class TenantDatabaseConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(TenantDatabaseConfigurer.class);
    
    private final DatabaseClient databaseClient;
    private final TenantResolver tenantResolver;

    public TenantDatabaseConfigurer(DatabaseClient databaseClient, TenantResolver tenantResolver) {
        this.databaseClient = databaseClient;
        this.tenantResolver = tenantResolver;
    }

    /**
     * Ejecuta una operación en el contexto del tenant actual (extraído del JWT)
     */
    public <T> Mono<T> executeInTenantContext(TenantDatabaseOperation<T> operation) {
        return tenantResolver.resolveSchemaName()
            .flatMap(schemaName -> executeInSchemaContext(schemaName, operation))
            .doOnError(error -> logger.error("Error ejecutando operación en contexto de tenant", error));
    }

    /**
     * Ejecuta una operación en el contexto de un esquema específico
     */
    public <T> Mono<T> executeInSchemaContext(String schemaName, TenantDatabaseOperation<T> operation) {
        logger.debug("Ejecutando operación en esquema: {}", schemaName);
        
        // Configurar search_path para el esquema del tenant
        return databaseClient.sql("SET search_path TO " + schemaName + ", public")
            .then()
            .then(Mono.defer(() -> {
                try {
                    return operation.execute(databaseClient);
                } catch (Exception e) {
                    return Mono.error(e);
                }
            }))
            .doOnSuccess(result -> logger.debug("Operación completada en esquema: {}", schemaName))
            .doOnError(error -> logger.error("Error en operación para esquema {}: {}", 
                                           schemaName, error.getMessage()));
    }

    /**
     * Ejecuta una operación directamente en el esquema 'public'
     * Útil para operaciones con la tabla de tenants
     */
    public <T> Mono<T> executeInPublicSchema(TenantDatabaseOperation<T> operation) {
        return executeInSchemaContext("public", operation);
    }

    /**
     * Configura el search_path para un esquema específico y devuelve el DatabaseClient
     * para uso inmediato. La configuración del search_path es temporal para la conexión actual.
     */
    public Mono<DatabaseClient> withSchemaContext(String schemaName) {
        return databaseClient.sql("SET search_path TO " + schemaName + ", public")
            .then()
            .thenReturn(databaseClient)
            .doOnSuccess(client -> logger.debug("DatabaseClient configurado para esquema: {}", schemaName));
    }

    /**
     * Configura el search_path para el tenant actual y devuelve el DatabaseClient
     */
    public Mono<DatabaseClient> withTenantContext() {
        return tenantResolver.resolveSchemaName()
            .flatMap(this::withSchemaContext);
    }

    /**
     * Interfaz funcional para operaciones de base de datos con contexto de tenant
     */
    @FunctionalInterface
    public interface TenantDatabaseOperation<T> {
        Mono<T> execute(DatabaseClient databaseClient);
    }
}