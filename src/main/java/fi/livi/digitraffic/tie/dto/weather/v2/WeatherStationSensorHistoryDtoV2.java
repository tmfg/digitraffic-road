package fi.livi.digitraffic.tie.dto.weather.v2;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationDataV1;
import fi.livi.digitraffic.tie.dto.v1.SensorValueHistoryDtoV1;
import fi.livi.digitraffic.tie.dto.weather.v1.WeatherStationSensorHistoryDtoV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "WeatherStationSensorValueHistoryV2", description = "Weather station's sensor value history.")
@JsonPropertyOrder({ "id", "dataUpdatedTime", "sensorValues" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherStationSensorHistoryDtoV2 extends StationDataV1<Long> {

    @Schema(description = "Weather station's sensor value history", requiredMode = Schema.RequiredMode.REQUIRED)
    public final List<SensorValueHistoryDtoV1> sensorValues;

    public WeatherStationSensorHistoryDtoV2(final Long stationId, final Instant dataUpdatedTime, final List<SensorValueHistoryDtoV1> sensorValues) {
        super(stationId, dataUpdatedTime);
        this.sensorValues = sensorValues;
    }

    public static WeatherStationSensorHistoryDtoV2 of(final WeatherStationSensorHistoryDtoV1 v1) {
        return new WeatherStationSensorHistoryDtoV2(v1.id, v1.dataUpdatedTime, v1.values);
    }

    @Override
    public boolean shouldContainLastModified() {
        return sensorValues != null && !sensorValues.isEmpty();
    }
}
