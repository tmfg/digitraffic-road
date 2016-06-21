package fi.livi.digitraffic.tie.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
