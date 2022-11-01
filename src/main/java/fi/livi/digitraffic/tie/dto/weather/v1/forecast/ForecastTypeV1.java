package fi.livi.digitraffic.tie.dto.weather.v1.forecast;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = ForecastTypeV1.API_DESCRIPTION)
public enum ForecastTypeV1 {
    OBSERVATION, FORECAST;

    public final static String API_DESCRIPTION = "Tells if object is an observation or a forecast. (OBSERVATION, FORECAST)";

    public static ForecastTypeV1 fromValue(final String value) {
        return ForecastTypeV1.valueOf(value.toUpperCase());
    }
}
