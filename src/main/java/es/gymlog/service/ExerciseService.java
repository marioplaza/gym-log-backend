package es.gymlog.service;

import es.gymlog.api.dto.CreateExerciseDTO;
import es.gymlog.api.dto.ExerciseDTO;
import es.gymlog.api.dto.UpdateExerciseDTO;
import es.gymlog.mapper.ExerciseMapper;
import es.gymlog.model.Exercise;
import es.gymlog.repository.ExerciseRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    public ExerciseService(ExerciseRepository exerciseRepository, ExerciseMapper exerciseMapper) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseMapper = exerciseMapper;
    }

    public Flux<ExerciseDTO> getAllExercises() {
        return exerciseRepository.findAll().map(exerciseMapper::toDto);
    }

    public Mono<ExerciseDTO> createExercise(CreateExerciseDTO dto) {
        Exercise exercise = exerciseMapper.toEntity(dto);
        exercise = new Exercise(
            UUID.randomUUID(),
            exercise.name(),
            exercise.description(),
            exercise.videoUrl(),
            exercise.targetMuscleGroup(),
            null, // TODO: Set createdByUserId from security context
            exercise.isPublic(),
            Instant.now(),
            Instant.now()
        );
        return exerciseRepository.save(exercise).map(exerciseMapper::toDto);
    }

    public Mono<ExerciseDTO> updateExercise(UUID id, UpdateExerciseDTO dto) {
        return exerciseRepository.findById(id)
            .flatMap(existingExercise -> {
                Exercise exerciseToUpdate = new Exercise(
                    existingExercise.id(),
                    dto.getName() != null ? dto.getName() : existingExercise.name(),
                    dto.getDescription() != null ? dto.getDescription() : existingExercise.description(),
                    dto.getVideoUrl() != null ? dto.getVideoUrl() : existingExercise.videoUrl(),
                    dto.getTargetMuscleGroup() != null ? dto.getTargetMuscleGroup() : existingExercise.targetMuscleGroup(),
                    existingExercise.createdByUserId(),
                    dto.getIsPublic() != null ? dto.getIsPublic() : existingExercise.isPublic(),
                    existingExercise.createdAt(),
                    Instant.now()
                );
                return exerciseRepository.save(exerciseToUpdate);
            })
            .map(exerciseMapper::toDto);
    }
}
