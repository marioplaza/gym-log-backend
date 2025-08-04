package es.gymlog.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad que representa un tenant (cliente/gimnasio) en la arquitectura multi-tenant.
 * Esta entidad se almacena en el esquema 'public' y contiene la información básica
 * de cada cliente, incluyendo el nombre del esquema de BD asignado.
 */
@Table("tenants")
public record Tenant(
    @Id
    UUID id,
    
    @Column("name")
    String name,
    
    @Column("email")
    String email,
    
    @Column("plan")
    TenantPlan plan,
    
    @Column("description")
    String description,
    
    @Column("address")
    String address,
    
    @Column("phone")
    String phone,
    
    @Column("schema_name")
    String schemaName,
    
    @Column("is_active")
    Boolean isActive,
    
    @Column("created_at")
    Instant createdAt,
    
    @Column("updated_at")
    Instant updatedAt
) {
    
    /**
     * Crea un nuevo tenant con valores por defecto
     */
    public static Tenant create(String name, String email, TenantPlan plan, String description, 
                               String address, String phone, String schemaName) {
        Instant now = Instant.now();
        return new Tenant(
            null, // Se generará automáticamente
            name,
            email,
            plan,
            description,
            address,
            phone,
            schemaName,
            true, // Activo por defecto
            now,
            now
        );
    }
    
    /**
     * Actualiza la fecha de modificación
     */
    public Tenant withUpdatedAt(Instant updatedAt) {
        return new Tenant(id, name, email, plan, description, address, phone, 
                         schemaName, isActive, createdAt, updatedAt);
    }
    
    /**
     * Actualiza el estado activo/inactivo
     */
    public Tenant withActive(boolean active) {
        return new Tenant(id, name, email, plan, description, address, phone, 
                         schemaName, active, createdAt, Instant.now());
    }
}

/**
 * Enum que representa los diferentes planes de suscripción disponibles
 */
enum TenantPlan {
    BASIC("basic"),
    PREMIUM("premium"),
    ENTERPRISE("enterprise");
    
    private final String value;
    
    TenantPlan(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static TenantPlan fromString(String value) {
        for (TenantPlan plan : TenantPlan.values()) {
            if (plan.value.equalsIgnoreCase(value)) {
                return plan;
            }
        }
        throw new IllegalArgumentException("Plan no válido: " + value);
    }
}