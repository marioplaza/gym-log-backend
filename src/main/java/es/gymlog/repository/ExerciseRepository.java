package es.gymlog.repository;

import es.gymlog.model.Exercise;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio para la gestión de datos de la entidad Exercise.
 */
@Repository
public interface ExerciseRepository extends ReactiveCrudRepository<Exercise, UUID> {
}
