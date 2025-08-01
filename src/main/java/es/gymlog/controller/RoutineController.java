package es.gymlog.controller;

import es.gymlog.api.RoutinesApi;
import es.gymlog.api.dto.CreateRoutineDTO;
import es.gymlog.api.dto.RoutineDTO;
import es.gymlog.api.dto.RoutineDayDTO;
import es.gymlog.api.dto.RoutineDetailDTO;
import es.gymlog.api.dto.UpdateRoutineDTO;
import es.gymlog.service.RoutineDayService;
import es.gymlog.service.RoutineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class RoutineController implements RoutinesApi {

    private final RoutineService routineService;
    private final RoutineDayService routineDayService;

    public RoutineController(RoutineService routineService, RoutineDayService routineDayService) {
        this.routineService = routineService;
        this.routineDayService = routineDayService;
    }

    @Override
    public Mono<ResponseEntity<RoutineDTO>> apiV1RoutinesPost(Mono<CreateRoutineDTO> createRoutineDTO, ServerWebExchange exchange) {
        return createRoutineDTO
            .flatMap(routineService::createRoutine)
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Override
    public Mono<ResponseEntity<Void>> apiV1RoutinesIdDelete(UUID id, ServerWebExchange exchange) {
        return routineService.deleteRoutine(id)
            .then(Mono.fromCallable(() -> ResponseEntity.noContent().build()));
    }

    @Override
    public Mono<ResponseEntity<RoutineDetailDTO>> apiV1RoutinesIdGet(UUID id, ServerWebExchange exchange) {
        // TODO: Implementar la obtención del detalle completo de la rutina
        return Mono.just(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Flux<RoutineDTO>>> apiV1RoutinesGet(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(routineService.getAllRoutines()));
    }

    @Override
    public Mono<ResponseEntity<RoutineDTO>> apiV1RoutinesIdPut(UUID id, Mono<UpdateRoutineDTO> updateRoutineDTO, ServerWebExchange exchange) {
        return updateRoutineDTO
            .flatMap(dto -> routineService.updateRoutine(id, dto))
            .map(ResponseEntity::ok);
    }
    
    // Este método no forma parte de la API generada.
    // Para activarlo, se debe definir el endpoint correspondiente en openapi.yml
    /* 
    public Mono<ResponseEntity<RoutineDayDTO>> addRoutineDay(UUID routineId, Mono<RoutineDayDTO> routineDayDTO, ServerWebExchange exchange) {
        return routineDayDTO
            .flatMap(dto -> routineDayService.addDayToRoutine(routineId, dto))
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }
    */
}
