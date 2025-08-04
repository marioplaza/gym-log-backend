package es.gymlog.config;

import es.gymlog.repository.TenantRepository;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;

/**
 * Gestor de migraciones Liquibase para arquitectura multi-tenant.
 * 
 * Este componente se ejecuta automáticamente al arrancar la aplicación y:
 * 1. Ejecuta migraciones en el esquema 'public' (tabla de tenants)
 * 2. Obtiene la lista de todos los tenants activos
 * 3. Itera sobre cada tenant y ejecuta migraciones en su esquema
 * 4. Crea esquemas que no existan automáticamente
 */
@Component
public class TenantLiquibaseManager implements SmartInitializingSingleton {

    private static final Logger logger = LoggerFactory.getLogger(TenantLiquibaseManager.class);
    
    private final DataSource dataSource;
    private final LiquibaseProperties liquibaseProperties;
    private final TenantRepository tenantRepository;

    public TenantLiquibaseManager(DataSource dataSource, 
                                 LiquibaseProperties liquibaseProperties,
                                 TenantRepository tenantRepository) {
        this.dataSource = dataSource;
        this.liquibaseProperties = liquibaseProperties;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public void afterSingletonsInstantiated() {
        logger.info("Iniciando gestión de migraciones multi-tenant...");
        
        try {
            // Paso 1: Ejecutar migraciones en esquema public (solo tabla tenants)
            runPublicSchemaMigrations();
            
            // Paso 2: Obtener lista de tenants y ejecutar migraciones en cada esquema
            runTenantMigrations();
            
            logger.info("Gestión de migraciones multi-tenant completada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error crítico durante la gestión de migraciones multi-tenant", e);
            throw new RuntimeException("Fallo en migraciones multi-tenant", e);
        }
    }

    /**
     * Ejecuta migraciones únicamente en el esquema 'public' para crear/actualizar
     * la tabla de tenants. Solo ejecuta el changeset específico de tenants.
     */
    private void runPublicSchemaMigrations() throws LiquibaseException, SQLException {
        logger.info("Ejecutando migraciones en esquema 'public'...");
        
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            // Configurar para usar esquema public
            database.setDefaultSchemaName("public");
            
            try (Liquibase liquibase = new Liquibase(
                    liquibaseProperties.getChangeLog(),
                    new ClassLoaderResourceAccessor(),
                    database)) {
                
                // Solo ejecutar changesets específicos para public (tenants)
                // Usamos contextos para filtrar - solo el changeset de tenants
                liquibase.update(new Contexts("public"), new LabelExpression());
                
                logger.info("Migraciones en esquema 'public' completadas");
            }
        }
    }

    /**
     * Obtiene la lista de tenants activos y ejecuta migraciones en cada esquema
     */
    private void runTenantMigrations() {
        logger.info("Obteniendo lista de tenants activos...");
        
        // Usar bloqueo reactivo para obtener los esquemas
        List<String> schemaNames = tenantRepository.findAllActiveSchemaNames()
            .collectList()
            .block(Duration.ofSeconds(30));
        
        if (schemaNames == null || schemaNames.isEmpty()) {
            logger.warn("No se encontraron tenants activos. Creando tenant demo por defecto...");
            createDemoTenantSchema();
            return;
        }
        
        logger.info("Encontrados {} tenants activos: {}", schemaNames.size(), schemaNames);
        
        // Ejecutar migraciones para cada esquema de tenant
        for (String schemaName : schemaNames) {
            try {
                runMigrationsForTenant(schemaName);
            } catch (Exception e) {
                logger.error("Error ejecutando migraciones para tenant '{}': {}", 
                           schemaName, e.getMessage(), e);
                // Continuar con el siguiente tenant en lugar de fallar completamente
            }
        }
    }

    /**
     * Ejecuta migraciones de Liquibase para un tenant específico
     */
    private void runMigrationsForTenant(String schemaName) throws LiquibaseException, SQLException {
        logger.info("Ejecutando migraciones para tenant '{}'...", schemaName);
        
        try (Connection connection = dataSource.getConnection()) {
            // Crear esquema si no existe
            createSchemaIfNotExists(connection, schemaName);
            
            Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            // Configurar para usar el esquema del tenant
            database.setDefaultSchemaName(schemaName);
            
            try (Liquibase liquibase = new Liquibase(
                    liquibaseProperties.getChangeLog(),
                    new ClassLoaderResourceAccessor(),
                    database)) {
                
                // Ejecutar todas las migraciones excepto las del contexto 'public'
                liquibase.update(new Contexts("tenant"), new LabelExpression());
                
                logger.info("Migraciones completadas para tenant '{}'", schemaName);
            }
        }
    }

    /**
     * Crea un esquema si no existe
     */
    private void createSchemaIfNotExists(Connection connection, String schemaName) throws SQLException {
        String createSchemaSQL = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(createSchemaSQL);
            logger.debug("Esquema '{}' verificado/creado", schemaName);
        }
    }

    /**
     * Crea el esquema para el tenant demo si no existen tenants
     */
    private void createDemoTenantSchema() {
        try (Connection connection = dataSource.getConnection()) {
            createSchemaIfNotExists(connection, "gym_demo");
            runMigrationsForTenant("gym_demo");
            logger.info("Esquema demo 'gym_demo' creado y migrado");
        } catch (Exception e) {
            logger.error("Error creando esquema demo", e);
        }
    }

    /**
     * Método público para ejecutar migraciones en un nuevo tenant
     * (usado cuando se crea un tenant dinámicamente)
     */
    public Mono<Void> runMigrationsForNewTenant(String schemaName) {
        return Mono.fromRunnable(() -> {
            try {
                logger.info("Ejecutando migraciones para nuevo tenant '{}'...", schemaName);
                runMigrationsForTenant(schemaName);
                logger.info("Migraciones completadas para nuevo tenant '{}'", schemaName);
            } catch (Exception e) {
                logger.error("Error ejecutando migraciones para nuevo tenant '{}'", schemaName, e);
                throw new RuntimeException("Error en migraciones para tenant: " + schemaName, e);
            }
        });
    }
}