package fi.livi.digitraffic.tie.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateHelpper {

    public static LocalDateTime getNewest(LocalDateTime first, LocalDateTime second) {
        if (first == null) {
            return second;
        } else if (second == null) {
            return first;
        }
        return first.isAfter(second) ? first: second;

    }

    public static LocalDate getNewest(LocalDate first, LocalDate second) {
        if (first == null) {
            return second;
        } else if (second == null) {
            return first;
        }
        return first.isAfter(second) ? first: second;
    }

}
