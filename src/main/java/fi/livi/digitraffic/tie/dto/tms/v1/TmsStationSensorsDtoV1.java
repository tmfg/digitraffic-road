package fi.livi.digitraffic.tie.dto.tms.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.RoadStationSensorsDtoV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Available sensors of weather stations")
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "roadStationSensors" })
public class TmsStationSensorsDtoV1 extends RoadStationSensorsDtoV1<TmsStationSensorDtoV1> {

    public TmsStationSensorsDtoV1(final Instant lastUpdated, final Instant dataLastCheckedTime, final List<TmsStationSensorDtoV1> roadStationSensors) {
        super(lastUpdated, dataLastCheckedTime, roadStationSensors);
    }
}
