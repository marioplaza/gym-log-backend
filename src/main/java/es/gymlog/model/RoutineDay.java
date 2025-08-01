package es.gymlog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa un día de entrenamiento dentro de una rutina.
 */
@Table("routine_days")
public record RoutineDay(
    @Id UUID id,
    UUID routineId,
    String name,
    int orderNum,
    Instant createdAt,
    Instant updatedAt
) {}
