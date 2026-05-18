package fi.livi.digitraffic.tie.dto.trafficmessage.v2;

/**
 * Date-time format constants for V2 traffic message API.
 */
public final class V2DateTimeFormat {

    /**
     * ISO 8601 date-time pattern with milliseconds and UTC Z suffix.
     * Produces format like: 2026-05-18T12:30:15.000Z
     */
    public static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String TIMEZONE = "UTC";

    private V2DateTimeFormat() {
    }
}

