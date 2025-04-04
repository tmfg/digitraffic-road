package fi.livi.digitraffic.tie.conf.jaxb2;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;

import fi.livi.digitraffic.common.util.TimeUtil;

/**
 * Converts xmlType="xs:dateTime" field as Instant (Z-time).
 */
public class DateTimeConverter {

    public static Instant parseDateTime(final String from) {
        try {
            if (StringUtils.isBlank(from)) {
                return null;
            }
            return ZonedDateTime.parse(from).toInstant();
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String printDateTime(final Instant from) {
        if (from == null) {
            return null;
        }
        return TimeUtil.toIsoDateTimeWithMillisAtUtc(from);
    }

    public static LocalDate parseDate(final String from) {
        try {
            if (StringUtils.isBlank(from)) {
                return null;
            }
            return LocalDate.parse(from);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String printDate(final LocalDate from) {
        if (from == null) {
            return null;
        }
        return from.toString();
    }
}
