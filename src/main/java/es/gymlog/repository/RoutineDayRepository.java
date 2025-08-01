package es.gymlog.repository;

import es.gymlog.model.RoutineDay;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repositorio para la gestión de datos de la entidad RoutineDay.
 */
@Repository
public interface RoutineDayRepository extends ReactiveCrudRepository<RoutineDay, UUID> {

    /**
     * Incrementa el order_num de todos los días de una rutina que tengan un order_num
     * mayor o igual al especificado.
     *
     * @param routineId   El ID de la rutina.
     * @param orderNum    El número de orden a partir del cual se incrementará.
     * @return Un Mono<Void> que se completa cuando la operación termina.
     */
    @Query("UPDATE routine_days SET order_num = order_num + 1 WHERE routine_id = :routineId AND order_num >= :orderNum")
    Mono<Void> incrementOrderNumFrom(UUID routineId, int orderNum);

    /**
     * Decrementa el order_num de todos los días de una rutina que tengan un order_num
     * mayor al especificado.
     *
     * @param routineId   El ID de la rutina.
     * @param orderNum    El número de orden a partir del cual se decrementará.
     * @return Un Mono<Void> que se completa cuando la operación termina.
     */
    @Query("UPDATE routine_days SET order_num = order_num - 1 WHERE routine_id = :routineId AND order_num > :orderNum")
    Mono<Void> decrementOrderNumFrom(UUID routineId, int orderNum);

    /**
     * Mueve el order_num de los días en un rango específico.
     *
     * @param routineId El ID de la rutina.
     * @param start     El inicio del rango.
     * @param end       El fin del rango.
     * @param shift     El valor a sumar (positivo o negativo).
     * @return Un Mono<Void> que se completa cuando la operación termina.
     */
    @Query("UPDATE routine_days SET order_num = order_num + :shift WHERE routine_id = :routineId AND order_num >= :start AND order_num <= :end")
    Mono<Void> shiftOrderNumInRange(UUID routineId, int start, int end, int shift);
}
