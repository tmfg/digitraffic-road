package fi.livi.digitraffic.tie.model.trafficmessage.datex2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SimppeliVersionTest {

    @Test
    public void newestSupportedIsTheLastConstant() {
        assertEquals(SimppeliVersion.values()[SimppeliVersion.values().length - 1],
                SimppeliVersion.newestSupported());
    }

    @Test
    public void versionsAreInAscendingOrder() {
        final SimppeliVersion[] versions = SimppeliVersion.values();
        for (int i = 1; i < versions.length; i++) {
            assertTrue(compare(versions[i - 1].version, versions[i].version) < 0,
                    versions[i - 1].version + " must be declared before " + versions[i].version);
        }
    }

    @Test
    public void listedVersionsAreSupported() {
        for (final SimppeliVersion version : SimppeliVersion.values()) {
            assertTrue(SimppeliVersion.isSupported(version.version));
        }
    }

    @Test
    public void versionsOutsideTheListAreNotSupported() {
        assertFalse(SimppeliVersion.isSupported("0.2.20"));
        assertFalse(SimppeliVersion.isSupported("0.3.0"));
        assertFalse(SimppeliVersion.isSupported("0.2.16"));
        assertFalse(SimppeliVersion.isSupported("0.2.19-beta"));
    }

    /** Compares dotted numeric versions part by part, so that "0.2.9" is older than "0.2.18". */
    private static int compare(final String a, final String b) {
        final String[] aParts = a.split("\\.");
        final String[] bParts = b.split("\\.");
        for (int i = 0; i < Math.max(aParts.length, bParts.length); i++) {
            final int result = Integer.compare(partAt(aParts, i), partAt(bParts, i));
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private static int partAt(final String[] parts, final int index) {
        return index < parts.length ? Integer.parseInt(parts[index].trim()) : 0;
    }
}
