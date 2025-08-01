package es.gymlog.mapper;

import es.gymlog.api.dto.ExerciseLogDTO;
import es.gymlog.model.ExerciseLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para convertir entre la entidad ExerciseLog y sus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = TimestampMapper.class)
public interface ExerciseLogMapper {

    ExerciseLogDTO toDto(ExerciseLog exerciseLog);

    @Mapping(target = "id", ignore = true) // Se genera uno nuevo
    @Mapping(target = "workoutSessionId", ignore = true) // Se establece en el servicio
    ExerciseLog toEntity(ExerciseLogDTO dto);
}
