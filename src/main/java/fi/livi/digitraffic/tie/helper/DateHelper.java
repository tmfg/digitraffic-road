package fi.livi.digitraffic.tie.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

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

    public static LocalDateTime toLocalDateTimeAtDefaultZone(XMLGregorianCalendar aika) {
        ZonedDateTime zonedDateTime = aika.toGregorianCalendar().toZonedDateTime();
        return ZonedDateTime.ofInstant(zonedDateTime.toInstant(), ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date toDateAtDefaultZone(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
