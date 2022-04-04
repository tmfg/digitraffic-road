package fi.livi.digitraffic.tie.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

public abstract class MqttUtil {
    public static Long getEpochSeconds(final ZonedDateTime time) {
        return time == null ? null : time.toEpochSecond();
    }

    public static double roundToScale(final double number, final int scale) {
        return new BigDecimal(number).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static String getTopicForMessage(final String topicString, final Object...topicParams) {
        return String.format(topicString, topicParams);
    }
}
