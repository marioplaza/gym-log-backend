package es.gymlog.mapper;

import es.gymlog.api.dto.UserDTO;
import es.gymlog.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "displayName", target = "name")
    @Mapping(target = "profileImageUrl", ignore = true)
    UserDTO toDto(User user);

    default OffsetDateTime map(Instant instant) {
        return instant != null ? instant.atOffset(ZoneOffset.UTC) : null;
    }
}
