package fi.livi.digitraffic.tie.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZonedDateTime;

public abstract class MqttUtil {
    public static Long getEpochSeconds(final ZonedDateTime time) {
        return time == null ? null : time.toEpochSecond();
    }

    public static Long getEpochSeconds(final Instant time) {
        return time == null ? null : time.getEpochSecond();
    }

    public static double roundToScale(final double number, final int scale) {
        return BigDecimal.valueOf(number).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static String getTopicForMessage(final String topicString, final Object...topicParams) {
        return String.format(topicString, topicParams);
    }
}
