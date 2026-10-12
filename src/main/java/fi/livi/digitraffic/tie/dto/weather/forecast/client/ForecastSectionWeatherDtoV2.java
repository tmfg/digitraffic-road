package fi.livi.digitraffic.tie.dto.weather.forecast.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastSectionWeatherDtoV2 {

    public final String naturalId;

    public final List<ForecastSectionForecastDtoV2> forecasts;

    public ForecastSectionWeatherDtoV2(@JsonProperty("id") final String naturalId,
                                       @JsonProperty("forecasts") final List<ForecastSectionForecastDtoV2> forecasts) {
        this.naturalId = naturalId;
        this.forecasts = forecasts == null ? List.of() : forecasts;
    }
}
