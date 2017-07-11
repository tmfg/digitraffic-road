package fi.livi.digitraffic.tie.metadata.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootMetadataObjectDto;
import fi.livi.digitraffic.tie.metadata.model.RoadStationSensor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Available sensors of weather stations")
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "roadStationSensors" })
public class RoadStationsSensorsMetadata extends RootMetadataObjectDto {

    @ApiModelProperty(value = "Available sensors of weather stations", required = true)
    private final List<RoadStationSensor> roadStationSensors;

    public RoadStationsSensorsMetadata(final List<RoadStationSensor> roadStationSensors, final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime) {
        super(lastUpdated, dataLastCheckedTime);
        this.roadStationSensors = roadStationSensors;
    }

    public List<RoadStationSensor> getRoadStationSensors() {
        return roadStationSensors;
    }
}
