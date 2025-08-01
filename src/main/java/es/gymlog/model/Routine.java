package es.gymlog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa una rutina de entrenamiento de un usuario.
 */
@Table("routines")
public record Routine(
    @Id UUID id,
    UUID userId,
    String name,
    boolean isActive,
    Instant createdAt,
    Instant updatedAt
) {}
