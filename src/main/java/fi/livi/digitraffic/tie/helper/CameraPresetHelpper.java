package fi.livi.digitraffic.tie.helper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CameraPresetHelpper {

    /** Presentation names that are set for unknown directions in Lotju */
    private static final Set<String> UNKNOWN_PRESET_NAMES =
            new HashSet(Arrays.asList(new String[] { "-", "–", "PUUTTUU"}));

    public static boolean isUnknownName(String name) {
        if (name == null) {
            return true;
        }
        return UNKNOWN_PRESET_NAMES.contains(name.trim().toUpperCase());
    }

    public static String fixName(String name) {
        if (isUnknownName(name)) {
            return null;
        }
        return name;
    }
}
