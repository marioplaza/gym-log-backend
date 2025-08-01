package es.gymlog.mapper;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Mapper para conversiones entre Instant y OffsetDateTime.
 * MapStruct utilizará esta clase para resolver las conversiones de fecha/hora.
 */
@Component
public class TimestampMapper {

    public OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant != null ? OffsetDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
    }

    public Instant toInstant(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toInstant() : null;
    }
}
