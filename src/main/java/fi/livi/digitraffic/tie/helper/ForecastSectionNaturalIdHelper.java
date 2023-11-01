package fi.livi.digitraffic.tie.helper;

public final class ForecastSectionNaturalIdHelper {

    private ForecastSectionNaturalIdHelper() {
    }

    public static int getRoadNumber(final String naturalId) {
        return Integer.parseInt(naturalId.substring(0, 5));
    }

    public static int getRoadSectionNumber(final String naturalId) {
        return Integer.parseInt(naturalId.substring(6, 9));
    }

    public static int getRoadSectionVersionNumber(final String naturalId) {
        return Integer.parseInt(naturalId.substring(10, 13));
    }
}