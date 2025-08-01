package es.gymlog.mapper;

import es.gymlog.api.dto.CreateExerciseDTO;
import es.gymlog.api.dto.ExerciseDTO;
import es.gymlog.api.dto.UpdateExerciseDTO;
import es.gymlog.model.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExerciseMapper {

    ExerciseDTO toDto(Exercise exercise);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    Exercise toEntity(CreateExerciseDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdByUserId", ignore = true)
    Exercise toEntity(UpdateExerciseDTO dto);
}
