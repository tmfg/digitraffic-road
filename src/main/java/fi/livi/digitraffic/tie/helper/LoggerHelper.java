package fi.livi.digitraffic.tie.helper;

/**
 * Provides helper functions to stringify objects for logging
 */
public class LoggerHelper {

    public static String objectToStringLoggerSafe(final Object value) {
        if (value != null) {
            return value.toString().replace("=", " = ");
        }
        return null;
    }
}
