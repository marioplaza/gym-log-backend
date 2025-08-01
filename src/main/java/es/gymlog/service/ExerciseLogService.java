package es.gymlog.service;

import es.gymlog.api.dto.CreateExerciseLogDTO;
import es.gymlog.api.dto.ExerciseLogDTO;
import es.gymlog.mapper.ExerciseLogMapper;
import es.gymlog.model.ExerciseLog;
import es.gymlog.repository.ExerciseLogRepository;
import es.gymlog.repository.WorkoutSessionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio para la gestión de los registros de ejercicios.
 */
@Service
public class ExerciseLogService {

    private final ExerciseLogRepository exerciseLogRepository;
    private final ExerciseLogMapper exerciseLogMapper;
    private final WorkoutSessionRepository workoutSessionRepository;

    public ExerciseLogService(ExerciseLogRepository exerciseLogRepository, ExerciseLogMapper exerciseLogMapper, WorkoutSessionRepository workoutSessionRepository) {
        this.exerciseLogRepository = exerciseLogRepository;
        this.exerciseLogMapper = exerciseLogMapper;
        this.workoutSessionRepository = workoutSessionRepository;
    }

    private Mono<UUID> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> (String) ctx.getAuthentication().getPrincipal())
            .map(UUID::fromString);
    }

    /**
     * Registra el rendimiento de un ejercicio en una sesión de entrenamiento.
     * @param sessionId El ID de la sesión de entrenamiento.
     * @param dto       DTO con los detalles del rendimiento.
     * @return DTO del registro creado.
     */
    public Mono<ExerciseLogDTO> logExercise(UUID sessionId, CreateExerciseLogDTO dto) {
        return getCurrentUserId().flatMap(userId ->
            workoutSessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("La sesión de entrenamiento no existe.")))
                .flatMap(session -> {
                    if (!session.userId().equals(userId)) {
                        return Mono.error(new AccessDeniedException("No tienes permiso para registrar en esta sesión."));
                    }

                    ExerciseLog log = new ExerciseLog(
                        UUID.randomUUID(),
                        sessionId,
                        dto.getRoutineExerciseId(),
                        dto.getSetsCompleted(),
                        dto.getRepsAchieved(),
                        dto.getWeightKg(),
                        dto.getNotes(),
                        Instant.now()
                    );
                    return exerciseLogRepository.save(log);
                })
                .map(exerciseLogMapper::toDto)
        );
    }
}
