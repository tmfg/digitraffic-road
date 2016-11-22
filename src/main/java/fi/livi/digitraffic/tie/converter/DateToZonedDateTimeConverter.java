package fi.livi.digitraffic.tie.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class DateToZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, Date> {
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Override
    public Date convertToDatabaseColumn(final ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null :new Date(zonedDateTime.toLocalDateTime().atZone(ZONE_ID).toInstant().toEpochMilli());
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(final Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZONE_ID).atZone(ZONE_ID);
    }
}