package es.gymlog.controller;

import es.gymlog.api.ExercisesApi;
import es.gymlog.api.dto.CreateExerciseDTO;
import es.gymlog.api.dto.ExerciseDTO;
import es.gymlog.api.dto.UpdateExerciseDTO;
import es.gymlog.service.ExerciseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class ExerciseController implements ExercisesApi {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @Override
    public Mono<ResponseEntity<ExerciseDTO>> apiV1ExercisesPost(Mono<CreateExerciseDTO> createExerciseDTO, ServerWebExchange exchange) {
        return createExerciseDTO
            .flatMap(exerciseService::createExercise)
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Override
    public Mono<ResponseEntity<Flux<ExerciseDTO>>> apiV1ExercisesGet(String name, String targetMuscleGroup, ServerWebExchange exchange) {
        // TODO: Aplicar filtros de búsqueda
        return Mono.just(ResponseEntity.ok(exerciseService.getAllExercises()));
    }

    @Override
    public Mono<ResponseEntity<ExerciseDTO>> apiV1ExercisesIdPut(UUID id, Mono<UpdateExerciseDTO> updateExerciseDTO, ServerWebExchange exchange) {
        return updateExerciseDTO
            .flatMap(dto -> exerciseService.updateExercise(id, dto))
            .map(ResponseEntity::ok);
    }
}
