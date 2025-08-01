package es.gymlog.mapper;

import es.gymlog.api.dto.WorkoutSessionDTO;
import es.gymlog.model.WorkoutSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper para convertir entre la entidad WorkoutSession y sus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = TimestampMapper.class)
public interface WorkoutSessionMapper {

    WorkoutSessionDTO toDto(WorkoutSession workoutSession);

    @Mapping(target = "userId", ignore = true) // El userId se establece en el servicio
    @Mapping(target = "id", ignore = true) // Se genera uno nuevo
    @Mapping(target = "endTime", ignore = true) // Se establece al finalizar la sesión
    WorkoutSession toEntity(WorkoutSessionDTO dto);
}
