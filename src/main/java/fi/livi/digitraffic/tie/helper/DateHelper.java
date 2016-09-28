package fi.livi.digitraffic.tie.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.xml.datatype.XMLGregorianCalendar;

public final class DateHelper {
    private DateHelper() {}

    public static LocalDateTime getNewest(final LocalDateTime first, final LocalDateTime second) {
        if (first == null) {
            return second;
        } else if(second == null || first.isAfter(second)) {
            return first;
        }

        return second;
    }

    public static LocalDate getNewest(final LocalDate first, final LocalDate second) {
        if (first == null) {
            return second;
        } else if (second == null || first.isAfter(second)) {
            return first;
        }

        return second;
    }

    public static LocalDateTime toLocalDateTimeAtZone(XMLGregorianCalendar aika, ZoneId toLocalDateTimeZoneId) {
        ZonedDateTime zonedDateTime = aika.toGregorianCalendar().toZonedDateTime();
        return ZonedDateTime.ofInstant(zonedDateTime.toInstant(), toLocalDateTimeZoneId).toLocalDateTime();
    }
}
