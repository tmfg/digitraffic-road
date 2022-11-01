package fi.livi.digitraffic.tie.dto.weather.v1.forecast;

import java.time.Instant;
import java.util.List;

import fi.livi.digitraffic.tie.dto.data.v1.DataDtoV1;
import io.swagger.v3.oas.annotations.media.Schema;

public class ForecastSectionsWeatherDtoV1 extends DataDtoV1 {

    @Schema(description = "Forecast sections")
    public final List<ForecastSectionWeatherDtoV1> forecastSections;

    public ForecastSectionsWeatherDtoV1(final Instant dataUpdatedTime) {
        this(dataUpdatedTime, null);
    }

    public ForecastSectionsWeatherDtoV1(final Instant dataUpdatedTime, final List<ForecastSectionWeatherDtoV1> forecastSections) {
        super(dataUpdatedTime);
        this.forecastSections = forecastSections;
    }
}
