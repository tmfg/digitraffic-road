package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForecastSectionDataDto {

    public final Timestamp messageTimestamp;

    public final List<ForecastSectionWeatherDto> forecastSectionWeatherList;

    public ForecastSectionDataDto(@JsonProperty("messageTimestamp") Timestamp messageTimestamp,
                                  @JsonProperty("roadConditions") List<ForecastSectionWeatherDto> forecastSectionWeatherList) {
        this.messageTimestamp = messageTimestamp;
        this.forecastSectionWeatherList = forecastSectionWeatherList;
    }
}