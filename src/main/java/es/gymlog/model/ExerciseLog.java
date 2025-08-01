package es.gymlog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Representa el registro de un ejercicio realizado en una sesión de entrenamiento.
 */
@Table("exercise_logs")
public record ExerciseLog(
    @Id UUID id,
    UUID workoutSessionId,
    UUID routineExerciseId,
    int setsCompleted,
    String repsAchieved,
    BigDecimal weightKg,
    String notes,
    Instant recordedAt
) {}
