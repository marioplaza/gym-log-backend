package es.gymlog.repository;

import es.gymlog.model.Routine;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Repositorio para la gestión de datos de la entidad Routine.
 * Proporciona métodos para interactuar con la tabla de rutinas en la base de datos
 * de forma reactiva.
 */
@Repository
public interface RoutineRepository extends ReactiveCrudRepository<Routine, UUID> {

    /**
     * Busca y devuelve todas las rutinas pertenecientes a un usuario específico.
     *
     * @param userId El UUID del usuario propietario de las rutinas.
     * @return Un Flux de rutinas que pertenecen al usuario especificado.
     */
    @Query("SELECT * FROM routines WHERE user_id = :userId")
    Flux<Routine> findByUserId(UUID userId);
}
