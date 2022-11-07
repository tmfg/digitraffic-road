package fi.livi.digitraffic.tie.helper;

public class LocationUtils {

    /**
     * This compares textual version strings of format A1.2.3 or 1.2.3
     * I.e.
     * compareTypesOrVersions("A1.2.3", "A1.2.3") == 0
     * compareTypesOrVersions("A1.2.3", "B1.2.3") == -1
     * compareTypesOrVersions("B1.2.3", "A1.2.3") == 1
     * compareTypesOrVersions("A1.1.3", "A1.2.3") == -1
     * compareTypesOrVersions("A1.3.3", "A1.2.3") == 1
     *
     * @param type1 first type string to compare
     * @param type2 second type string to compare
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     */
    public static int compareTypesOrVersions(final String type1, final String type2) {

        final String version1Alpha = getAlpha(type1);
        final String version2Alpha = getAlpha(type2);
        final int alphaResult = version1Alpha.compareTo(version2Alpha);
        if (alphaResult != 0) {
            return alphaResult;
        }

        final String[] versions1 = getNumericVersions(type1);
        final String[] versions2 = getNumericVersions(type2);

        final int maxLengthOfVersionSplits = Math.max(versions1.length, versions2.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++){
            final Integer v1 = i < versions1.length ? Integer.parseInt(versions1[i]) : 0;
            final Integer v2 = i < versions2.length ? Integer.parseInt(versions2[i]) : 0;
            final int compare = v1.compareTo(v2);
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }

    private static String[] getNumericVersions(final String versionString) {
        // Remove all not matched in the list -> removes all but numbers and dots
        final String onlyNumbers = versionString.replaceAll("[^\\d.]", "");
        // Split by dot delimiter
        return onlyNumbers.split("\\.", 20);
    }

    private static String getAlpha(final String versionString) {
        return versionString.split("\\d+", 2)[0];
    }
}
