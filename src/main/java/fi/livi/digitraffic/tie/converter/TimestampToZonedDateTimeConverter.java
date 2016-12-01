package fi.livi.digitraffic.tie.converter;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class TimestampToZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, java.sql.Timestamp> {
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Override
    public Timestamp convertToDatabaseColumn(final ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null : Timestamp.from(zonedDateTime.toInstant());
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(final Timestamp timestamp) {
        return timestamp == null ? null : ZonedDateTime.ofInstant(timestamp.toInstant(), ZONE_ID);
    }
}