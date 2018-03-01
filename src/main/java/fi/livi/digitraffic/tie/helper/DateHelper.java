package fi.livi.digitraffic.tie.helper;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DateHelper {

    private static final Logger log = LoggerFactory.getLogger(DateHelper.class);

    private DateHelper() {}

    public static ZonedDateTime getNewest(final ZonedDateTime first, final ZonedDateTime second) {
        if (first == null) {
            return second;
        } else if(second == null || first.isAfter(second)) {
            return first;
        }

        return second;
    }

    public static LocalDateTime toLocalDateTime(final XMLGregorianCalendar calendar) {
        if (calendar != null) {
            return calendar.toGregorianCalendar().toZonedDateTime().withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }

    /**
     * Needed because some fields in db are Oracle Date type and Date won't have millis.
     */
    public static ZonedDateTime toZonedDateTimeWithoutMillis(final XMLGregorianCalendar calendar)  {
        if (calendar != null) {
            try {
                final XMLGregorianCalendar calSeconds =
                        DatatypeFactory.newInstance().newXMLGregorianCalendar(
                                calendar.getYear(),
                                calendar.getMonth(),
                                calendar.getDay(),
                                calendar.getHour(),
                                calendar.getMinute(),
                                calendar.getSecond(),
                                0,
                                calendar.getTimezone());
                return toZonedDateTime(calSeconds);
            } catch (DatatypeConfigurationException e) {
                throw new RuntimeException("Failed to convert XMLGregorianCalendar " + calendar + " to XMLGregorianCalendar with out millis.", e);
            }
        }
        return null;
    }

    public static ZonedDateTime toZonedDateTime(final XMLGregorianCalendar calendar) {
        if (calendar != null) {
            // This way Time is formed as 1995-01-01T00:00+02:00[Europe/Helsinki]
            //                 and not as 1995-01-01T00:00+02:00[GMT+02:00]
            // HashCodeBuilder handles them differently
            return ZonedDateTime.of(toLocalDateTime(calendar), ZoneId.systemDefault());
        }
        return null;
    }

    public static ZonedDateTime toZonedDateTime(final Instant instant) {
        return instant == null ? null : instant.atZone(UTC);
    }

    public static ZonedDateTime toZonedDateTime(final LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return localDateTime.atZone(ZoneId.systemDefault());
        }
        return null;
    }

    public static LocalDateTime toLocalDateTime(final ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }

    public static Date toDate(final ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            return Date.from(zonedDateTime.toInstant());
        }
        return null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            final GregorianCalendar gregorianCalendar = GregorianCalendar.from(zonedDateTime);
            try {
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
            } catch (DatatypeConfigurationException e) {
                log.error("Failed to convert ZonedDateTime " + zonedDateTime + " to XMLGregorianCalendar", e);
            }
        }
        return null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendarUtc(final ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            final ZonedDateTime utc = ZonedDateTime.ofInstant(zonedDateTime.toInstant(), ZoneOffset.UTC);
            final GregorianCalendar gregorianCalendar = GregorianCalendar.from(utc);
            try {
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
            } catch (DatatypeConfigurationException e) {
                log.error("Failed to convert ZonedDateTime " + zonedDateTime + " to XMLGregorianCalendar", e);
            }
        }
        return null;
    }

    public static LocalDateTime toLocalDateTime(final long aika) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(aika), ZoneId.systemDefault());
    }
}
