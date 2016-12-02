package fi.livi.digitraffic.tie.helper;

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

    public static LocalDateTime toLocalDateTime(XMLGregorianCalendar calendar) {
        if (calendar != null) {
            ZonedDateTime zonedDateTime = calendar.toGregorianCalendar().toZonedDateTime();
            return ZonedDateTime.ofInstant(zonedDateTime.toInstant(), ZoneId.systemDefault()).toLocalDateTime();
        }
        return null;
    }

    public static ZonedDateTime toZonedDateTime(XMLGregorianCalendar calendar) {
        if (calendar != null) {
            // This way Time is formed as 1995-01-01T00:00+02:00[Europe/Helsinki]
            //                 and not as 1995-01-01T00:00+02:00[GMT+02:00]
            // HashCodeBuilder handles them differently
            return ZonedDateTime.of(toLocalDateTime(calendar), ZoneId.systemDefault());
        }
        return null;
    }

    public static ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return localDateTime.atZone(ZoneId.systemDefault());
        }
        return null;
    }

    public static LocalDateTime toLocalDateTime(ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            return zonedDateTime.toLocalDateTime();
        }
        return null;
    }

    public static Date toDate(ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            return Date.from(zonedDateTime.toInstant());
        }
        return null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            GregorianCalendar gregorianCalendar = GregorianCalendar.from(zonedDateTime);
            try {
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
            } catch (DatatypeConfigurationException e) {
                log.error("Failed to convert ZonedDateTime " + zonedDateTime + " to XMLGregorianCalendar", e);
            }
        }
        return null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendarUtc(ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            ZonedDateTime utc = ZonedDateTime.ofInstant(zonedDateTime.toInstant(), ZoneOffset.UTC);
            GregorianCalendar gregorianCalendar = GregorianCalendar.from(utc);
            try {
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
            } catch (DatatypeConfigurationException e) {
                log.error("Failed to convert ZonedDateTime " + zonedDateTime + " to XMLGregorianCalendar", e);
            }
        }
        return null;
    }
}
