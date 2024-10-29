package fi.livi.digitraffic.tie.dto.roadstation.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.SensorValueDtoV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
@JsonPropertyOrder({ "id", "dataUpdatedTime" })
public abstract class AbstractStationDataWithSensorsDtoV1 extends StationDataV1<Long> {

    @Schema(description = "Measured sensor values of the station", requiredMode = Schema.RequiredMode.REQUIRED)
    public final List<SensorValueDtoV1> sensorValues;

    public AbstractStationDataWithSensorsDtoV1(final long roadStationNaturalId, final Instant updated,
                                               final List<SensorValueDtoV1> sensorValues) {
        super(roadStationNaturalId, updated);
        this.sensorValues = sensorValues;
    }

    @Override
    public boolean shouldContainLastModified() {
        return sensorValues != null && !sensorValues.isEmpty();
    }
}
