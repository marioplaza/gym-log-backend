package es.gymlog.service;

import es.gymlog.api.dto.CreateWorkoutSessionDTO;
import es.gymlog.api.dto.WorkoutSessionDTO;
import es.gymlog.mapper.WorkoutSessionMapper;
import es.gymlog.model.WorkoutSession;
import es.gymlog.repository.WorkoutSessionRepository;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio para la gestión de las sesiones de entrenamiento.
 */
@Service
public class WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutSessionMapper workoutSessionMapper;

    public WorkoutSessionService(WorkoutSessionRepository workoutSessionRepository, WorkoutSessionMapper workoutSessionMapper) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutSessionMapper = workoutSessionMapper;
    }

    private Mono<UUID> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> (String) ctx.getAuthentication().getPrincipal())
            .map(UUID::fromString);
    }

    /**
     * Inicia una nueva sesión de entrenamiento para el usuario autenticado.
     * @param dto DTO con la información inicial de la sesión.
     * @return DTO de la sesión creada.
     */
    public Mono<WorkoutSessionDTO> startSession(CreateWorkoutSessionDTO dto) {
        return getCurrentUserId().flatMap(userId -> {
            WorkoutSession session = new WorkoutSession(
                UUID.randomUUID(),
                userId,
                dto.getRoutineDayId(),
                Instant.now(),
                null, // end_time se establece al finalizar
                dto.getNotes()
            );
            return workoutSessionRepository.save(session)
                .map(workoutSessionMapper::toDto);
        });
    }
}
