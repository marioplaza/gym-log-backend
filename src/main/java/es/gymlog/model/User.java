package es.gymlog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa un usuario de la aplicación.
 */
@Table("users")
public record User(
    @Id UUID id,
    String providerId,
    String provider,
    String email,
    String displayName,
    Instant createdAt,
    Instant updatedAt
) {}
