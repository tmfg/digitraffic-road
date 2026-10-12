package fi.livi.digitraffic.tie.dto.weather.forecast.v2;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.common.dto.data.v1.DataUpdatedSupportV1;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "id", "forecasts" })
@Schema(description = "Weather forecasts of a detailed forecast section")
public class ForecastSectionWeatherDetailedDtoV2 implements DataUpdatedSupportV1 {

    @Schema(description = ForecastSectionsWeatherDtoV2.ID_DESCRIPTION)
    @JsonProperty("id")
    public final String id;

    @Schema(description = "Weather forecasts of the forecast section")
    public final List<ForecastSectionWeatherForecastDtoV2> forecasts;

    private final Instant dataUpdatedTime;

    public ForecastSectionWeatherDetailedDtoV2(final String naturalId, final List<ForecastSectionWeatherForecastDtoV2> forecasts,
                                               final Instant dataUpdatedTime) {
        this.id = naturalId;
        this.forecasts = forecasts;
        this.dataUpdatedTime = dataUpdatedTime;
    }

    @Override
    public Instant getDataUpdatedTime() {
        return dataUpdatedTime;
    }
}
