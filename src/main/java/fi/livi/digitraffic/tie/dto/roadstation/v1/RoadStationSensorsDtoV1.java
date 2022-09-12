package fi.livi.digitraffic.tie.dto.roadstation.v1;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Road station sensors metadata")
public abstract class RoadStationSensorsDtoV1<T extends RoadStationSensorDtoV1> extends StationMetadataDtoV1  {

    @Schema(description = "Available sensors of road stations", required = true)
    public final List<T> sensors;

    public RoadStationSensorsDtoV1(final Instant dataUpdatedTime, final Instant dataLastCheckedTime, final List<T> sensors) {
        super(dataUpdatedTime, dataLastCheckedTime);
        this.sensors = sensors;
    }
}
