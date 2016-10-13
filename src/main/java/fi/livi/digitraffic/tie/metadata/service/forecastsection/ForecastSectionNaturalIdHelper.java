package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.util.regex.Pattern;

public final class ForecastSectionNaturalIdHelper {

    private ForecastSectionNaturalIdHelper() {
    }

    public static int getRoadNumber(String naturalId) {
        validateNaturalId(naturalId);
        return Integer.parseInt(naturalId.substring(0, 5));
    }

    public static int getRoadSectionNumber(String naturalId) {
        validateNaturalId(naturalId);
        return Integer.parseInt(naturalId.substring(6, 9));
    }

    public static int getRoadSectionVersionNumber(String naturalId) {
        validateNaturalId(naturalId);
        return Integer.parseInt(naturalId.substring(10, 13));
    }

    public static boolean isNaturalId(String naturalId) {
        return Pattern.matches("^\\d{5}\\_\\d{3}\\_\\d{3}\\_\\d$", naturalId);
    }

    private static void validateNaturalId(String naturalId) {
        if (!isNaturalId(naturalId)) {
            throw new RuntimeException("Invalid ForecastSectionNaturalId: " + naturalId);
        }
    }
}