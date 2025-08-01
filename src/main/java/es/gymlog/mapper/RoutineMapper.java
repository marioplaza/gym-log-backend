package es.gymlog.mapper;

import es.gymlog.api.dto.CreateRoutineDTO;
import es.gymlog.api.dto.RoutineDTO;
import es.gymlog.model.Routine;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper para convertir entre la entidad Routine y sus DTOs.
 * MapStruct generará la implementación de esta interfaz.
 * Se configura para ser un componente de Spring, permitiendo su inyección.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoutineMapper {

    /**
     * Convierte una entidad Routine a su representación en DTO.
     *
     * @param routine La entidad a convertir.
     * @return El DTO correspondiente.
     */
    RoutineDTO toDto(Routine routine);

    /**
     * Convierte un DTO de creación a una entidad Routine.
     *
     * @param dto El DTO de creación.
     * @return La entidad Routine correspondiente.
     */
    Routine toEntity(CreateRoutineDTO dto);
}
