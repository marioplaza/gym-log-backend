package es.gymlog.repository;

import es.gymlog.model.WorkoutSession;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositorio para la gestión de datos de la entidad WorkoutSession.
 */
@Repository
public interface WorkoutSessionRepository extends ReactiveCrudRepository<WorkoutSession, UUID> {
}
