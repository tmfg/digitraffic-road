package fi.livi.digitraffic.tie.helper;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
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

    public static ZonedDateTime toZonedDateTimeAtUtc(final XMLGregorianCalendar calendar) {
        return calendar == null ? null : toZonedDateTimeAtUtc(calendar.toGregorianCalendar().toInstant());
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
                return toZonedDateTimeAtUtc(calSeconds);
            } catch (final DatatypeConfigurationException e) {
                throw new IllegalArgumentException("Failed to convert XMLGregorianCalendar " + calendar + " to XMLGregorianCalendar with out millis.", e);
            }
        }
        return null;
    }

    public static ZonedDateTime toZonedDateTimeAtUtc(final ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null : toZonedDateTimeAtUtc(zonedDateTime.toInstant());
    }

    public static ZonedDateTime toZonedDateTimeAtUtc(final Instant instant) {
        return instant == null ? null : instant.atZone(UTC);
    }

    public static ZonedDateTime toZonedDateTimeAtUtc(final long epochMillis) {
        return toZonedDateTimeAtUtc(Instant.ofEpochMilli(epochMillis));
    }


    public static ZonedDateTime toZonedDateTimeAtUtc(final Date from) {
        return from == null ? null : toZonedDateTimeAtUtc(from.toInstant());
    }

    public static ZonedDateTime zonedDateTimeNowAtUtc() {
        return toZonedDateTimeAtUtc(Instant.now());
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
            final ZonedDateTime utc = ZonedDateTime.ofInstant(zonedDateTime.toInstant(), UTC);
            final GregorianCalendar gregorianCalendar = GregorianCalendar.from(utc);
            try {
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
            } catch (DatatypeConfigurationException e) {
                log.error("Failed to convert ZonedDateTime " + zonedDateTime + " to XMLGregorianCalendar", e);
            }
        }
        return null;
    }
}
