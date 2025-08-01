package es.gymlog.controller;

import es.gymlog.api.WorkoutSessionsApi;
import es.gymlog.api.dto.CreateExerciseLogDTO;
import es.gymlog.api.dto.CreateWorkoutSessionDTO;
import es.gymlog.api.dto.ExerciseLogDTO;
import es.gymlog.api.dto.WorkoutSessionDTO;
import es.gymlog.service.ExerciseLogService;
import es.gymlog.service.WorkoutSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class WorkoutSessionController implements WorkoutSessionsApi {

    private final WorkoutSessionService workoutSessionService;
    private final ExerciseLogService exerciseLogService;

    public WorkoutSessionController(WorkoutSessionService workoutSessionService, ExerciseLogService exerciseLogService) {
        this.workoutSessionService = workoutSessionService;
        this.exerciseLogService = exerciseLogService;
    }

    @Override
    public Mono<ResponseEntity<WorkoutSessionDTO>> apiV1WorkoutSessionsPost(Mono<CreateWorkoutSessionDTO> createWorkoutSessionDTO, ServerWebExchange exchange) {
        return createWorkoutSessionDTO
            .flatMap(workoutSessionService::startSession)
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Override
    public Mono<ResponseEntity<ExerciseLogDTO>> apiV1WorkoutSessionsSessionIdLogsPost(UUID sessionId, Mono<CreateExerciseLogDTO> createExerciseLogDTO, ServerWebExchange exchange) {
        return createExerciseLogDTO
            .flatMap(dto -> exerciseLogService.logExercise(sessionId, dto))
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }
}
