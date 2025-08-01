package es.gymlog.model;

import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Representa la relación entre un usuario y un rol.
 */
@Table("user_roles")
public record UserRole(
    UUID userId,
    Integer roleId
) {}
