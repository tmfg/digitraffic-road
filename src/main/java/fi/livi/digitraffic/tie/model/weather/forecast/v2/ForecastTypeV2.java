package fi.livi.digitraffic.tie.model.weather.forecast.v2;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = ForecastTypeV2.API_DESCRIPTION)
public enum ForecastTypeV2 {
    OBSERVATION, FORECAST;

    public static final String API_DESCRIPTION = "Tells if object is an observation or a forecast. (OBSERVATION, FORECAST)";

    public static ForecastTypeV2 of(final String value) {
        if (value == null) {
            return null;
        }
        for (final ForecastTypeV2 type : values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
