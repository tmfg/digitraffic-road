package fi.livi.digitraffic.tie.dto.tms.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.AbstractStationDataWithSensorsDtoV1;
import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TMS station data with sensor values")
@JsonPropertyOrder({ "id", "tmsNumber", "dataUpdatedTime" })
public class TmsStationDataDtoV1 extends AbstractStationDataWithSensorsDtoV1 {

    @Schema(description = "TMS station number", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(value = "tmsNumber")
    public final long tmsStationNaturalId;

    public TmsStationDataDtoV1(final Long roadStationNaturalId, final Long tmsStationNaturalId,
                               final Instant stationLatestUpdated,
                               final List<SensorValueDtoV1> sensorValues) {
        super(roadStationNaturalId, stationLatestUpdated, sensorValues);
        this.tmsStationNaturalId = tmsStationNaturalId;
    }
}
