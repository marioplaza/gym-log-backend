package es.gymlog.repository;

import es.gymlog.entity.Tenant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repositorio reactivo para la gestión de tenants.
 * 
 * IMPORTANTE: Este repositorio siempre opera sobre el esquema 'public'
 * ya que la tabla 'tenants' se encuentra allí y no en los esquemas de tenant.
 */
@Repository
public interface TenantRepository extends ReactiveCrudRepository<Tenant, UUID> {
    
    /**
     * Busca un tenant por su email
     */
    Mono<Tenant> findByEmail(String email);
    
    /**
     * Busca un tenant por el nombre de su esquema
     */
    Mono<Tenant> findBySchemaName(String schemaName);
    
    /**
     * Obtiene todos los tenants activos
     */
    Flux<Tenant> findByIsActiveTrue();
    
    /**
     * Verifica si existe un tenant con el email dado
     */
    Mono<Boolean> existsByEmail(String email);
    
    /**
     * Verifica si existe un tenant con el nombre de esquema dado
     */
    Mono<Boolean> existsBySchemaName(String schemaName);
    
    /**
     * Obtiene solo los nombres de esquema de todos los tenants activos.
     * Esta consulta es específicamente para el TenantLiquibaseManager.
     */
    @Query("SELECT schema_name FROM public.tenants WHERE is_active = true")
    Flux<String> findAllActiveSchemaNames();
    
    /**
     * Cuenta el número total de tenants activos
     */
    @Query("SELECT COUNT(*) FROM public.tenants WHERE is_active = true")
    Mono<Long> countActiveTenantsCustom();
}