package es.gymlog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa un ejercicio dentro de un día de entrenamiento de una rutina.
 */
@Table("routine_exercises")
public record RoutineExercise(
    @Id UUID id,
    UUID routineDayId,
    UUID exerciseId,
    int orderNum,
    boolean isActive,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {}
