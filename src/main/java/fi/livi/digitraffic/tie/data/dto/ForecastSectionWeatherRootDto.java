package fi.livi.digitraffic.tie.data.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public class ForecastSectionWeatherRootDto extends RootDataObjectDto {

    public final List<ForecastSectionWeatherDataDto> weatherData;

    public ForecastSectionWeatherRootDto(ZonedDateTime timestamp, List<ForecastSectionWeatherDataDto> weatherData) {
        super(timestamp);
        this.weatherData = weatherData;
    }

    public ForecastSectionWeatherRootDto(LocalDateTime localTimestamp, List<ForecastSectionWeatherDataDto> weatherData) {
        super(localTimestamp);
        this.weatherData = weatherData;
    }
}
