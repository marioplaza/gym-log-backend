package es.gymlog.mapper;

import es.gymlog.api.dto.RoutineDayDTO;
import es.gymlog.model.RoutineDay;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper para convertir entre la entidad RoutineDay y sus DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoutineDayMapper {

    RoutineDayDTO toDto(RoutineDay routineDay);

    RoutineDay toEntity(RoutineDayDTO dto);
}
