package fi.livi.digitraffic.tie.converter;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import fi.livi.digitraffic.tie.helper.DateHelper;

@Converter(autoApply = true)
public class ZonedDateTimeToSqlTimestampAttributeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(final ZonedDateTime zonedDateTime) {
        return DateHelper.toSqlTimestamp(zonedDateTime);
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(final Timestamp sqlTimestamp) {
        return DateHelper.toZonedDateTimeAtUtc(sqlTimestamp);
    }
}