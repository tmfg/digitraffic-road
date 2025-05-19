package fi.livi.digitraffic.tie.dto.weather.forecast.client;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForecastSectionDataDto {

    public final Instant messageTimestamp;

    public final List<ForecastSectionWeatherDto> forecastSectionWeatherList;

    public ForecastSectionDataDto(
            @JsonProperty("messageTimestamp")
            final Instant messageTimestamp,
            @JsonProperty("roadConditions")
            final List<ForecastSectionWeatherDto> forecastSectionWeatherList) {
        this.messageTimestamp = messageTimestamp;
        this.forecastSectionWeatherList = forecastSectionWeatherList;
    }
}
