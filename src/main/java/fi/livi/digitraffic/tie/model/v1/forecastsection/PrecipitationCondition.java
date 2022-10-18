package fi.livi.digitraffic.tie.model.v1.forecastsection;

import static fi.livi.digitraffic.tie.model.v1.forecastsection.PrecipitationCondition.API_DESCRIPTION;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = API_DESCRIPTION)
public enum PrecipitationCondition {
    NO_DATA_AVAILABLE(0),
    NO_RAIN_DRY_WEATHER(1),
    LIGHT_RAIN(2),
    RAIN(3),
    HEAVY_RAIN(4),
    LIGHT_SNOWFALL(5),
    SNOWFALL(6),
    HEAVY_SNOWFALL(7);

    private final int value;

    PrecipitationCondition(final int value) {
        this.value = value;
    }

    public static PrecipitationCondition of(final Integer value) {
        if (value == null) {
            return null;
        }
        for (PrecipitationCondition precipitationCondition : values()) {
            if (precipitationCondition.value == value) {
                return precipitationCondition;
            }
        }
        return null;
    }

    public static final String API_DESCRIPTION =
        "Precipitation condition:<br>\n" +
        "0 = no data available,<br>\n" +
        "1 = rain intensity lt 0.2 mm/h,<br>\n" +
        "2 = rain intensity ge 0.2 mm/h,<br>\n" +
        "3 = rain intensity ge 2.5 mm/h,<br>\n" +
        "4 = rain intensity ge 7.6 mm/h,<br>\n" +
        "5 = snowing intensity ge 0.2 cm/h,<br>\n" +
        "6 = snowing intensity ge 1 cm/h,<br>\n" +
        "7 = snowing intensity ge 3 cm/h<br>\n" +
        "(lt = lower than, ge = greater or equal)";
}
