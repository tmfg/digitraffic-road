package fi.livi.digitraffic.tie.helper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class DataValidyHelper {
    /** Field values that are set for unknown in Lotju */
    private static final Set<String> UNKNOWN_VALUES = Arrays.stream(new String[] { "-", "–", "PUUTTUU"}).collect(Collectors.toSet());

    private DataValidyHelper() {}

    public static boolean isUnknownValue(final String value) {
        return value == null || UNKNOWN_VALUES.contains(value.trim().toUpperCase());
    }

    public static String nullifyUnknownValue(final String name) {
        return isUnknownValue(name) ? null : name;
    }
}
