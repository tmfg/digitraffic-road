package fi.livi.digitraffic.tie.metadata.dto;

import java.time.ZonedDateTime;
import java.util.List;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Available sensors of weather stations")
public class RoadStationsSensorsMetadata extends RootDataObjectDto {

    @ApiModelProperty(value = "Available sensors of weather stations", required = true)
    private final List<RoadStationSensor> roadStationSensors;

    public RoadStationsSensorsMetadata(final List<RoadStationSensor> roadStationSensors, final ZonedDateTime lastUpdated) {
        super(lastUpdated);
        this.roadStationSensors = roadStationSensors;
    }

    public List<RoadStationSensor> getRoadStationSensors() {
        return roadStationSensors;
    }
}
