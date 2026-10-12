package fi.livi.digitraffic.tie.dto.weather.forecast.client;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastSectionDataDtoV2 {

    public final Instant dataUpdatedTime;

    public final List<ForecastSectionWeatherDtoV2> forecastSections;

    public ForecastSectionDataDtoV2(
            @JsonProperty("dataUpdatedTime")
            final Instant dataUpdatedTime,
            @JsonProperty("forecastSections")
            final List<ForecastSectionWeatherDtoV2> forecastSections) {
        this.dataUpdatedTime = dataUpdatedTime;
        this.forecastSections = forecastSections == null ? List.of() : forecastSections;
    }
}
