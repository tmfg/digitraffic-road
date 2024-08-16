package fi.livi.digitraffic.tie.converter;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import fi.livi.digitraffic.common.util.TimeUtil;

@Converter(autoApply = true)
public class ZonedDateTimeToSqlTimestampAttributeConverter implements AttributeConverter<ZonedDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(final ZonedDateTime zonedDateTime) {
        return TimeUtil.toSqlTimestamp(zonedDateTime);
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(final Timestamp sqlTimestamp) {
        return TimeUtil.toZonedDateTimeAtUtc(sqlTimestamp);
    }
}
