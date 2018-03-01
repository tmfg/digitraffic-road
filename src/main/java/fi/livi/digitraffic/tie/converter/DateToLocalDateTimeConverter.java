package fi.livi.digitraffic.tie.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.persistence.AttributeConverter;

//@Converter(autoApply = true)
public class DateToLocalDateTimeConverter implements AttributeConverter<LocalDateTime, Date> {
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Override
    public Date convertToDatabaseColumn(final LocalDateTime localDateTime) {
        return localDateTime == null ? null :new Date(localDateTime.atZone(ZONE_ID).toInstant().toEpochMilli());
    }

    @Override
    public LocalDateTime convertToEntityAttribute(final Date date) {
        return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZONE_ID);
    }
}