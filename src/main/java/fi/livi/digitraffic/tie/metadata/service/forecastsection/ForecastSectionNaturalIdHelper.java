package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.math.BigDecimal;

public class ForecastSectionNaturalIdHelper {

    public static int getRoadNumber(String naturalId) {
        return Integer.parseInt(naturalId.substring(0, 5));
    }

    public static int getRoadSectionNumber(String naturalId) {
        return Integer.parseInt(naturalId.substring(6, 9));
    }

    public static BigDecimal getRoadNumAndSectionNum(String naturalId) {
        return new BigDecimal(getRoadNumber(naturalId) + "." + getRoadSectionNumber(naturalId));
    }

    public static int getRoadSectionVersionNumber(String naturalId) {
        return Integer.parseInt(naturalId.substring(10, 13));
    }
}