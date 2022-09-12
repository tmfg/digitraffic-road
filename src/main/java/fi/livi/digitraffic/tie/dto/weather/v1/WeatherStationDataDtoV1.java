package fi.livi.digitraffic.tie.dto.weather.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.AbstractStationDataWithSensorsDtoV1;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weather station data with sensor values")
@JsonPropertyOrder({ "id", "dataUpdatedTime" })
public class WeatherStationDataDtoV1 extends AbstractStationDataWithSensorsDtoV1 {

    public WeatherStationDataDtoV1(final Long roadStationNaturalId,
                                   final Instant stationLatestMeasurement,
                                   final List<SensorValueDtoV1> sensorValues) {
        super(roadStationNaturalId, stationLatestMeasurement, sensorValues);
    }
}
