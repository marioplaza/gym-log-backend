package es.gymlog.repository;

import es.gymlog.model.ExerciseLog;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio para la gestión de datos de la entidad ExerciseLog.
 */
@Repository
public interface ExerciseLogRepository extends ReactiveCrudRepository<ExerciseLog, UUID> {
}
