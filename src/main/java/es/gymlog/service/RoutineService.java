package es.gymlog.service;

import es.gymlog.api.dto.CreateRoutineDTO;
import es.gymlog.api.dto.RoutineDTO;
import es.gymlog.api.dto.UpdateRoutineDTO;
import es.gymlog.mapper.RoutineMapper;
import es.gymlog.model.Routine;
import es.gymlog.repository.RoutineRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio para la gestión de rutinas de entrenamiento.
 * Contiene la lógica de negocio para crear, leer, actualizar y eliminar rutinas,
 * asegurando que los usuarios solo puedan acceder y modificar sus propios datos.
 */
@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final RoutineMapper routineMapper;

    /**
     * Constructor para la inyección de dependencias.
     *
     * @param routineRepository El repositorio para el acceso a datos de las rutinas.
     * @param routineMapper     El mapper para convertir entre entidades y DTOs.
     */
    public RoutineService(RoutineRepository routineRepository, RoutineMapper routineMapper) {
        this.routineRepository = routineRepository;
        this.routineMapper = routineMapper;
    }

    /**
     * Obtiene el ID del usuario actualmente autenticado desde el contexto de seguridad reactivo.
     *
     * @return Un Mono que emite el UUID del usuario.
     */
    private Mono<UUID> getCurrentUserId() {
        // En un entorno real, esto extraería el 'subject' o 'sub' del token JWT
        // que correspondería al UUID de nuestro usuario en la base de datos.
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> (String) ctx.getAuthentication().getPrincipal())
            .map(UUID::fromString);
    }

    /**
     * Comprueba si una rutina pertenece al usuario especificado.
     *
     * @param routine La rutina a comprobar.
     * @param userId  El ID del usuario con el que se debe comparar.
     * @return Un Mono que emite la rutina si la comprobación es exitosa, o emite un error de acceso denegado.
     */
    private Mono<Routine> checkOwnership(Routine routine, UUID userId) {
        if (routine.userId().equals(userId)) {
            return Mono.just(routine);
        } else {
            return Mono.error(new AccessDeniedException("El usuario no tiene permiso para acceder a esta rutina."));
        }
    }

    /**
     * Recupera todas las rutinas pertenecientes al usuario autenticado.
     *
     * @return Un Flux de DTOs de las rutinas del usuario.
     */
    public Flux<RoutineDTO> getAllRoutines() {
        return getCurrentUserId()
            .flatMapMany(userId -> routineRepository.findByUserId(userId))
            .map(routineMapper::toDto);
    }

    /**
     * Crea una nueva rutina para el usuario autenticado.
     *
     * @param dto El DTO con los datos para la nueva rutina.
     * @return Un Mono que emite el DTO de la rutina creada.
     */
    public Mono<RoutineDTO> createRoutine(CreateRoutineDTO dto) {
        return getCurrentUserId().flatMap(userId -> {
            Routine routine = new Routine(
                UUID.randomUUID(),
                userId,
                dto.getName(),
                dto.getIsActive(),
                Instant.now(),
                Instant.now()
            );
            return routineRepository.save(routine).map(routineMapper::toDto);
        });
    }

    /**
     * Actualiza una rutina existente, verificando primero que pertenece al usuario autenticado.
     *
     * @param id  El ID de la rutina a actualizar.
     * @param dto El DTO con los datos a actualizar.
     * @return Un Mono que emite el DTO de la rutina actualizada.
     */
    public Mono<RoutineDTO> updateRoutine(UUID id, UpdateRoutineDTO dto) {
        return getCurrentUserId().flatMap(userId ->
            routineRepository.findById(id)
                .flatMap(existingRoutine -> checkOwnership(existingRoutine, userId))
                .flatMap(ownedRoutine -> {
                    Routine routineToUpdate = new Routine(
                        ownedRoutine.id(),
                        ownedRoutine.userId(),
                        dto.getName() != null ? dto.getName() : ownedRoutine.name(),
                        dto.getIsActive() != null ? dto.getIsActive() : ownedRoutine.isActive(),
                        ownedRoutine.createdAt(),
                        Instant.now()
                    );
                    return routineRepository.save(routineToUpdate);
                })
                .map(routineMapper::toDto)
        );
    }

    /**
     * Elimina una rutina, verificando primero que pertenece al usuario autenticado.
     *
     * @param id El ID de la rutina a eliminar.
     * @return Un Mono<Void> que se completa cuando la operación termina.
     */
    public Mono<Void> deleteRoutine(UUID id) {
        return getCurrentUserId().flatMap(userId ->
            routineRepository.findById(id)
                .flatMap(routine -> checkOwnership(routine, userId))
                .flatMap(routineRepository::delete)
        );
    }
}
