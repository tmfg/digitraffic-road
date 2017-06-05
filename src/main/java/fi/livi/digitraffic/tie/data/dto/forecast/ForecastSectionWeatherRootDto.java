package fi.livi.digitraffic.tie.data.dto.forecast;

import java.time.ZonedDateTime;
import java.util.List;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;

public class ForecastSectionWeatherRootDto extends RootDataObjectDto {
    public final List<ForecastSectionWeatherDataDto> weatherData;

    public ForecastSectionWeatherRootDto(final ZonedDateTime timestamp, final List<ForecastSectionWeatherDataDto> weatherData) {
        super(timestamp);
        this.weatherData = weatherData;
    }
}
