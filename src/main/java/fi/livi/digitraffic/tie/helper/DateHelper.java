package fi.livi.digitraffic.tie.helper;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DateHelper {

    private static final Logger log = LoggerFactory.getLogger(DateHelper.class);


    // Tue, 03 Sep 2019 13:56:36 GMT
    private final static ZoneId GMT = ZoneId.of("GMT");
    public static final String LAST_MODIFIED_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final DateTimeFormatter LAST_MODIFIED_FORMATTER =
        DateTimeFormatter.ofPattern(LAST_MODIFIED_FORMAT, Locale.US).withZone(GMT);

    public static final DateTimeFormatter ISO_DATE_TIME_WITH_MILLIS_AT_UTC;
    static {
        ISO_DATE_TIME_WITH_MILLIS_AT_UTC = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendLiteral('.')
            .appendFraction(MILLI_OF_SECOND, 3, 3, false)
            .appendLiteral('Z')
            .toFormatter()
            .withZone(UTC);
    }

    private DateHelper() {}

    public static String getInLastModifiedHeaderFormat(final Instant instant) {
        return LAST_MODIFIED_FORMATTER.format(instant);
    }

    public static ZonedDateTime getNewestAtUtc(final ZonedDateTime first, final ZonedDateTime second) {
        if (first == null) {
            return toZonedDateTimeAtUtc(second);
        } else if(second == null || first.isAfter(second)) {
            return toZonedDateTimeAtUtc(first);
        }
        return toZonedDateTimeAtUtc(second);
    }

    public static Instant getNewest(final Instant first, final Instant second) {
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

    public static Instant toInstant(final XMLGregorianCalendar calendar) {
        return calendar == null ? null : calendar.toGregorianCalendar().toInstant();
    }

    public static Instant toInstant(final ZonedDateTime from) {
        return from != null ? from.toInstant() : null;
    }

    public static Instant toInstant(final long epochMillis) {
        return Instant.ofEpochMilli(epochMillis);
    }

    public static Instant toInstantWithOutMillis(final long epochMillis) {
        return withoutMillis(Instant.ofEpochMilli(epochMillis));
    }

    public static Instant toInstantWithOutMillis(final ZonedDateTime measuredTime) {
        if (measuredTime == null) {
            return null;
        }
        return withoutMillis(Instant.ofEpochSecond(measuredTime.toEpochSecond()));
    }

    /**
     * Needed because some fields in db are Oracle Date type and Date won't have millis.
     */
    public static ZonedDateTime toZonedDateTimeWithoutMillisAtUtc(final XMLGregorianCalendar calendar)  {
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

    public static XMLGregorianCalendar toXMLGregorianCalendarAtUtc(final ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return toXMLGregorianCalendarAtUtc(zonedDateTime.toInstant());
    }

    public static XMLGregorianCalendar toXMLGregorianCalendarAtUtc(final Instant from) {
        if (from != null) {
            final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(from.toEpochMilli());
            try {
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
            } catch (DatatypeConfigurationException e) {
                log.error("Failed to convert Instant " + from + " to XMLGregorianCalendar", e);
            }
        }
        return null;
    }

    public static Instant getNowWithoutNanos() {
        return withoutNanos(Instant.now());
    }

    public static Instant getNowWithoutMillis() {
        return withoutMillis(Instant.now());
    }
    public static ZonedDateTime getZonedDateTimeNowAtUtc() {
        return toZonedDateTimeAtUtc(Instant.now());
    }

    public static ZonedDateTime getZonedDateTimeNowWithoutMillisAtUtc() {
        return withoutMillisAtUtc(toZonedDateTimeAtUtc(Instant.now()));
    }

    public static Instant withoutNanos(final Instant from) {
        if (from != null) {
            return Instant.ofEpochMilli(from.toEpochMilli());
        }
        return null;
    }

    public static Instant withoutMillis(final Instant from) {
        if (from != null) {
            return Instant.ofEpochSecond(from.getEpochSecond());
        }
        return null;
    }

    public static ZonedDateTime withoutMillisAtUtc(final ZonedDateTime from) {
        if (from != null) {
            return toZonedDateTimeAtUtc(from.with(MILLI_OF_SECOND, 0));
        }
        return null;
    }

    public static ZonedDateTime toZonedDateTimeWithoutMillisAtUtc(final Instant from) {
        if (from != null) {
            return toZonedDateTimeAtUtc(withoutMillis(from));
        }
        return null;
    }

    public static String toIsoDateTimeWithMillisAtUtc(final Instant from) {
        return ISO_DATE_TIME_WITH_MILLIS_AT_UTC.format(from);
    }

    public static Timestamp toSqlTimestamp(final ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null : Timestamp.from(zonedDateTime.toInstant());
    }

    public static Instant appendMillis(final Instant time, final long millis) {
        if (time == null) {
            return null;
        }
        return time.plusMillis(millis);
    }

    public static Instant toInstant(final java.sql.Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    public static Instant roundToSeconds(final Instant from) {
        if ( from == null) {
            return null;
        }
        return Instant.ofEpochSecond(from.getEpochSecond() + (from.getNano() >= 500000000 ? 1 : 0));
    }

    public static String isoToHttpDate(String yearMonthDayIso) throws ParseException {
        SimpleDateFormat yearMonthDayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat httpDate = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDate.setTimeZone(TimeZone.getTimeZone("GMT"));
        return httpDate.format(yearMonthDayFormat.parse(yearMonthDayIso));
    }
}
