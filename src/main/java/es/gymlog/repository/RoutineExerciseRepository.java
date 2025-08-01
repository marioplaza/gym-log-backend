package es.gymlog.repository;

import es.gymlog.model.RoutineExercise;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio para la gestión de datos de la entidad RoutineExercise.
 */
@Repository
public interface RoutineExerciseRepository extends ReactiveCrudRepository<RoutineExercise, UUID> {
}
