package es.gymlog.service;

import es.gymlog.api.dto.RoutineDayDTO;
import es.gymlog.mapper.RoutineDayMapper;
import es.gymlog.model.RoutineDay;
import es.gymlog.repository.RoutineDayRepository;
import es.gymlog.repository.RoutineRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio para la gestión de los días de una rutina.
 * Contiene la lógica para añadir, modificar y eliminar días de una rutina,
 * incluyendo la reordenación automática.
 */
@Service
public class RoutineDayService {

    private final RoutineRepository routineRepository;
    private final RoutineDayRepository routineDayRepository;
    private final RoutineDayMapper routineDayMapper;
    private final TransactionalOperator transactionalOperator;

    public RoutineDayService(RoutineRepository routineRepository, RoutineDayRepository routineDayRepository, RoutineDayMapper routineDayMapper, TransactionalOperator transactionalOperator) {
        this.routineRepository = routineRepository;
        this.routineDayRepository = routineDayRepository;
        this.routineDayMapper = routineDayMapper;
        this.transactionalOperator = transactionalOperator;
    }

    private Mono<UUID> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> (String) ctx.getAuthentication().getPrincipal())
            .map(UUID::fromString);
    }

    /**
     * Añade un nuevo día a una rutina, aplicando la lógica de reordenación.
     */
    public Mono<RoutineDayDTO> addDayToRoutine(UUID routineId, RoutineDayDTO dto) {
        return getCurrentUserId()
            .flatMap(userId -> routineRepository.findById(routineId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("La rutina no existe.")))
                .flatMap(routine -> {
                    if (!routine.userId().equals(userId)) {
                        return Mono.error(new AccessDeniedException("No tienes permiso para modificar esta rutina."));
                    }
                    return Mono.just(routine);
                })
            )
            .flatMap(routine -> {
                RoutineDay newDay = new RoutineDay(UUID.randomUUID(), routineId, dto.getName(), dto.getOrderNum(), Instant.now(), Instant.now());
                Mono<RoutineDay> operation = routineDayRepository.incrementOrderNumFrom(routineId, dto.getOrderNum())
                    .then(routineDayRepository.save(newDay));
                return transactionalOperator.transactional(operation);
            })
            .map(routineDayMapper::toDto);
    }

    /**
     * Elimina un día de una rutina, reajustando el orden de los días posteriores.
     */
    public Mono<Void> deleteRoutineDay(UUID dayId) {
        return getCurrentUserId().flatMap(userId ->
            routineDayRepository.findById(dayId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El día de rutina no existe.")))
                .flatMap(day ->
                    routineRepository.findById(day.routineId())
                        .flatMap(routine -> {
                            if (!routine.userId().equals(userId)) {
                                return Mono.error(new AccessDeniedException("No tienes permiso para eliminar este día de rutina."));
                            }
                            return Mono.just(day);
                        })
                )
                .flatMap(day -> {
                    Mono<Void> operation = routineDayRepository.delete(day)
                        .then(routineDayRepository.decrementOrderNumFrom(day.routineId(), day.orderNum()));
                    return transactionalOperator.transactional(operation);
                })
        );
    }
    
    /**
     * Actualiza un día de rutina, incluyendo su posición (order_num), con lógica transaccional.
     */
    public Mono<RoutineDayDTO> updateRoutineDay(UUID dayId, RoutineDayDTO dto) {
        return getCurrentUserId().flatMap(userId ->
            routineDayRepository.findById(dayId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El día de rutina no existe.")))
                .flatMap(day ->
                    routineRepository.findById(day.routineId())
                        .flatMap(routine -> {
                            if (!routine.userId().equals(userId)) {
                                return Mono.error(new AccessDeniedException("No tienes permiso para modificar este día de rutina."));
                            }
                            return Mono.just(day);
                        })
                )
                .flatMap(existingDay -> {
                    final int oldOrder = existingDay.orderNum();
                    final int newOrder = dto.getOrderNum();
                    final UUID routineId = existingDay.routineId();

                    Mono<Void> reorderOperation = Mono.empty();
                    if (oldOrder != newOrder) {
                        if (newOrder < oldOrder) { // Mover hacia arriba
                            reorderOperation = routineDayRepository.shiftOrderNumInRange(routineId, newOrder, oldOrder - 1, 1);
                        } else { // Mover hacia abajo
                            reorderOperation = routineDayRepository.shiftOrderNumInRange(routineId, oldOrder + 1, newOrder, -1);
                        }
                    }

                    RoutineDay updatedDay = new RoutineDay(
                        existingDay.id(),
                        existingDay.routineId(),
                        dto.getName(),
                        newOrder,
                        existingDay.createdAt(),
                        Instant.now()
                    );
                    
                    return transactionalOperator.transactional(
                        reorderOperation.then(routineDayRepository.save(updatedDay))
                    );
                })
                .map(routineDayMapper::toDto)
        );
    }
}
