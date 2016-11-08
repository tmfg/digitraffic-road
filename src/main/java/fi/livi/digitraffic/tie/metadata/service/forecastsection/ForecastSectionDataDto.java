package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.List;

public class ForecastSectionDataDto {

    public final ZonedDateTime messageTimestamp;

    public final List<ForecastSectionWeatherDto> forecastSectionWeatherList;

    public ForecastSectionDataDto(@JsonProperty("messageTimestamp") ZonedDateTime messageTimestamp,
                                  @JsonProperty("roadConditions") List<ForecastSectionWeatherDto> forecastSectionWeatherList) {
        this.messageTimestamp = messageTimestamp;
        this.forecastSectionWeatherList = forecastSectionWeatherList;
    }
}