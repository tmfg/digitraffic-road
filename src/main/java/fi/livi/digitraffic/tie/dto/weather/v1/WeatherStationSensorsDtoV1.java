package fi.livi.digitraffic.tie.dto.weather.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.RoadStationSensorsDtoV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Available sensors of weather stations")
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "roadStationSensors" })
public class WeatherStationSensorsDtoV1 extends RoadStationSensorsDtoV1<WeatherStationSensorDtoV1> {

    public WeatherStationSensorsDtoV1(final Instant lastUpdated, final Instant dataLastCheckedTime, final List<WeatherStationSensorDtoV1> roadStationSensors) {
        super(lastUpdated, dataLastCheckedTime, roadStationSensors);
    }
}
