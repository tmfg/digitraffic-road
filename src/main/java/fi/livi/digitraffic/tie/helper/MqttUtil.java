package fi.livi.digitraffic.tie.helper;

import java.time.ZonedDateTime;

public abstract class MqttUtil {
    public static Long getEpochSeconds(final ZonedDateTime time) {
        return time == null ? null : time.toEpochSecond();
    }
}
