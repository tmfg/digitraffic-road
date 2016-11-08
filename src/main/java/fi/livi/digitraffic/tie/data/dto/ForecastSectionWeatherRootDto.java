package fi.livi.digitraffic.tie.data.dto;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.List;

public class ForecastSectionWeatherRootDto {

    @ApiModelProperty(value = "When weather data was updated last time")
    public final LocalDateTime updated;

    public final List<ForecastSectionWeatherDataDto> weatherData;

    public ForecastSectionWeatherRootDto(LocalDateTime updated, List<ForecastSectionWeatherDataDto> weatherData) {
        this.updated = updated;
        this.weatherData = weatherData;
    }
}
