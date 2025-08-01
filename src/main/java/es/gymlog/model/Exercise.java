package es.gymlog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa un ejercicio en el banco de ejercicios.
 */
@Table("exercises")
public record Exercise(
    @Id UUID id,
    String name,
    String description,
    String videoUrl,
    String targetMuscleGroup,
    UUID createdByUserId,
    boolean isPublic,
    Instant createdAt,
    Instant updatedAt
) {}
