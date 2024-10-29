package fi.livi.digitraffic.tie.dto.v1;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Available sensors of weather stations")
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "roadStationSensors" })
public class TmsRoadStationsSensorsMetadata extends RootMetadataObjectDto {

    @Schema(description = "Available sensors of weather stations", requiredMode = Schema.RequiredMode.REQUIRED)
    private final List<TmsRoadStationSensorDto> roadStationSensors;

    public TmsRoadStationsSensorsMetadata(final List<TmsRoadStationSensorDto> roadStationSensors, final ZonedDateTime lastUpdated, final ZonedDateTime dataLastCheckedTime) {
        super(lastUpdated, dataLastCheckedTime);
        this.roadStationSensors = roadStationSensors;
    }

    public List<TmsRoadStationSensorDto> getRoadStationSensors() {
        return roadStationSensors;
    }
}
