package fi.livi.digitraffic.tie.helper;

import java.util.Arrays;
import java.util.List;

public final class CameraPresetHelper {
    /** Presentation names that are set for unknown directions in Lotju */
    private static final List<String> UNKNOWN_PRESET_NAMES = Arrays.asList("-", "–", "PUUTTUU");

    private CameraPresetHelper() {}

    public static boolean isUnknownName(final String name) {
        return name == null || UNKNOWN_PRESET_NAMES.contains(name.trim().toUpperCase());
    }

    public static String fixName(final String name) {
        return isUnknownName(name) ? null : name;
    }
}
