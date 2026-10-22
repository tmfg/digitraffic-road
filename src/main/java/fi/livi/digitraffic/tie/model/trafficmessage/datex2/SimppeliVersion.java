package fi.livi.digitraffic.tie.model.trafficmessage.datex2;

import java.util.Arrays;

/**
 * SIMPPELI message versions this code is known to handle.
 *
 * <p>Constants must stay in ascending version order: {@link #newestSupported()} relies on the
 * declaration order.</p>
 */
public enum SimppeliVersion {
    V_0_2_17("0.2.17"),
    V_0_2_18("0.2.18"),
    V_0_2_19("0.2.19");

    public final String version;

    SimppeliVersion(final String version) {
        this.version = version;
    }

    /** The newest version this code is known to handle. */
    public static SimppeliVersion newestSupported() {
        return values()[values().length - 1];
    }

    /**
     * Tells whether the given version is one of the listed ones.
     *
     * @param version dotted numeric version, e.g. "0.2.19"
     */
    public static boolean isSupported(final String version) {
        return Arrays.stream(values()).anyMatch(v -> v.version.equals(version));
    }
}
