package fi.livi.digitraffic.tie.metadata.dto;

import java.util.List;

import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Available sensors of road weather stations")
public class RoadStationsSensorsMetadata {

    @ApiModelProperty(value = "Available sensors of road weather stations", required = true)
    private final List<RoadStationSensor> roadStationSensors;

    public RoadStationsSensorsMetadata(final List<RoadStationSensor> roadStationSensors) {
        this.roadStationSensors = roadStationSensors;
    }

    public List<RoadStationSensor> getRoadStationSensors() {
        return roadStationSensors;
    }
}
