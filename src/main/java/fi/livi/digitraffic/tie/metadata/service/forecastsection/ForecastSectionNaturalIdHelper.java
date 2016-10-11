package fi.livi.digitraffic.tie.metadata.service.forecastsection;

public final class ForecastSectionNaturalIdHelper {

    private ForecastSectionNaturalIdHelper() {
    }

    public static int getRoadNumber(String naturalId) {
        return Integer.parseInt(naturalId.substring(0, 5));
    }

    public static int getRoadSectionNumber(String naturalId) {
        return Integer.parseInt(naturalId.substring(6, 9));
    }

    public static int getRoadSectionVersionNumber(String naturalId) {
        return Integer.parseInt(naturalId.substring(10, 13));
    }

    public static boolean equalsIgnoreVersion(String naturalId1, String naturalId2) {
        return getRoadNumber(naturalId1) == getRoadNumber(naturalId2) &&
               getRoadSectionNumber(naturalId1) == getRoadSectionNumber(naturalId2);
    }
}