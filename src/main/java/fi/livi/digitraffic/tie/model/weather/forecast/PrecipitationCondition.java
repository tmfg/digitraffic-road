package fi.livi.digitraffic.tie.model.weather.forecast;

import static fi.livi.digitraffic.tie.model.weather.forecast.PrecipitationCondition.API_DESCRIPTION;

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
        for (final PrecipitationCondition precipitationCondition : values()) {
            if (precipitationCondition.value == value) {
                return precipitationCondition;
            }
        }
        return null;
    }

    public static final String API_DESCRIPTION = """
                    Precipitation condition:<br>
                    0 = no data available,<br>
                    1 = rain intensity lt 0.2 mm/h,<br>
                    2 = rain intensity ge 0.2 mm/h,<br>
                    3 = rain intensity ge 2.5 mm/h,<br>
                    4 = rain intensity ge 7.6 mm/h,<br>
                    5 = snowing intensity ge 0.2 cm/h,<br>
                    6 = snowing intensity ge 1 cm/h,<br>
                    7 = snowing intensity ge 3 cm/h<br>
                    (lt = lower than, ge = greater or equal)""";
}
