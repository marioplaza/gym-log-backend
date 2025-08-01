package es.gymlog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa una sesión de entrenamiento de un usuario.
 */
@Table("workout_sessions")
public record WorkoutSession(
    @Id UUID id,
    UUID userId,
    UUID routineDayId,
    Instant startTime,
    Instant endTime,
    String notes
) {}
