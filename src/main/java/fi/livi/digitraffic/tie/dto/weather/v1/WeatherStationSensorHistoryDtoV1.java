package fi.livi.digitraffic.tie.dto.weather.v1;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationDataV1;
import fi.livi.digitraffic.tie.dto.weather.WeatherSensorValueHistoryDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(name = "SensorValueHistory", description = "Weather station's sensor value history.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherStationSensorHistoryDtoV1 extends StationDataV1<Long> {

    @Schema(description = "Weather station's sensor value history", requiredMode = Schema.RequiredMode.REQUIRED)
    public final List<WeatherSensorValueHistoryDto> values;

    public WeatherStationSensorHistoryDtoV1(final Long stationId, final Instant dataUpdatedTime, final List<WeatherSensorValueHistoryDto> values) {
        super(stationId, dataUpdatedTime);
        this.values = values;
    }

    @Override
    public boolean shouldContainLastModified() {
        return values != null && !values.isEmpty();
    }
}
