package fi.livi.digitraffic.tie.dto.trafficmessage.v2;

/**
 * Date-time format constants for V2 traffic message API.
 */
public final class V2DateTimeFormat {

    /**
     * ISO 8601 date-time pattern with optional milliseconds and UTC Z suffix.
     * Serializes as: 2026-05-18T12:30:15.000Z (always with millis since Instant always has nanos)
     * Deserializes both: 2026-05-18T12:30:15.000Z and 2026-05-18T12:30:15Z
     */
    public static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'";

    public static final String TIMEZONE = "UTC";

    private V2DateTimeFormat() {
    }
}

