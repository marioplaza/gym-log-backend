package es.gymlog.config;

import es.gymlog.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Resolvedor de tenant que extrae el tenantId del JWT y resuelve el esquema correspondiente.
 * 
 * Este componente es fundamental para la arquitectura multi-tenant ya que:
 * 1. Extrae el tenantId del JWT de cada petición
 * 2. Consulta la tabla de tenants para obtener el schema_name
 * 3. Proporciona el contexto de tenant para el resto de la aplicación
 */
@Component
public class TenantResolver {

    private static final Logger logger = LoggerFactory.getLogger(TenantResolver.class);
    
    private final TenantRepository tenantRepository;

    public TenantResolver(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /**
     * Resuelve el contexto de tenant desde el JWT de la petición actual
     */
    public Mono<TenantContext> resolveTenantContext() {
        return ReactiveSecurityContextHolder.getContext()
            .cast(org.springframework.security.core.context.SecurityContext.class)
            .map(securityContext -> securityContext.getAuthentication())
            .cast(JwtAuthenticationToken.class)
            .map(jwtAuthenticationToken -> {
                Jwt jwt = jwtAuthenticationToken.getToken();
                String tenantIdStr = jwt.getClaim("tenantId");
                
                if (tenantIdStr == null || tenantIdStr.isEmpty()) {
                    logger.warn("JWT no contiene tenantId válido");
                    throw new IllegalStateException("JWT debe contener tenantId");
                }
                
                return UUID.fromString(tenantIdStr);
            })
            .flatMap(this::resolveTenantContextById)
            .doOnNext(context -> logger.debug("Contexto de tenant resuelto: {}", context.schemaName()))
            .doOnError(error -> logger.error("Error resolviendo contexto de tenant", error));
    }

    /**
     * Resuelve el contexto de tenant por ID
     */
    public Mono<TenantContext> resolveTenantContextById(UUID tenantId) {
        return tenantRepository.findById(tenantId)
            .map(tenant -> new TenantContext(
                tenant.id(),
                tenant.name(),
                tenant.schemaName()
            ))
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Tenant no encontrado: " + tenantId)));
    }

    /**
     * Resuelve solo el nombre del esquema desde el JWT (método optimizado)
     */
    public Mono<String> resolveSchemaName() {
        return resolveTenantContext()
            .map(TenantContext::schemaName);
    }

    /**
     * Extrae el tenantId del JWT sin hacer consultas adicionales
     */
    public Mono<UUID> extractTenantId() {
        return ReactiveSecurityContextHolder.getContext()
            .cast(org.springframework.security.core.context.SecurityContext.class)
            .map(securityContext -> securityContext.getAuthentication())
            .cast(JwtAuthenticationToken.class)
            .map(jwtAuthenticationToken -> {
                Jwt jwt = jwtAuthenticationToken.getToken();
                String tenantIdStr = jwt.getClaim("tenantId");
                
                if (tenantIdStr == null || tenantIdStr.isEmpty()) {
                    throw new IllegalStateException("JWT debe contener tenantId");
                }
                
                return UUID.fromString(tenantIdStr);
            });
    }

    /**
     * Método para obtener contexto de tenant para endpoints públicos (con tenantId en header)
     * Útil para endpoints como creación de tenant donde no hay JWT aún
     */
    public Mono<TenantContext> resolveTenantFromHeader(String tenantIdHeader) {
        if (tenantIdHeader == null || tenantIdHeader.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Header X-Tenant-ID requerido"));
        }
        
        try {
            UUID tenantId = UUID.fromString(tenantIdHeader);
            return resolveTenantContextById(tenantId);
        } catch (IllegalArgumentException e) {
            return Mono.error(new IllegalArgumentException("X-Tenant-ID debe ser un UUID válido"));
        }
    }

    /**
     * Record que representa el contexto de un tenant
     */
    public record TenantContext(
        UUID tenantId,
        String tenantName,
        String schemaName
    ) {}
}