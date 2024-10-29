package fi.livi.digitraffic.tie.dto.roadstation.v1;

import java.io.Serializable;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamPresetDataV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Measurement data from road station", subTypes = { WeathercamPresetDataV1.class })
@JsonPropertyOrder({ "id", "measuredTime" })
public class StationMeasurementDataV1<ID_TYPE> implements Serializable {

    @Schema(description = "Sensor id", requiredMode = Schema.RequiredMode.REQUIRED)
    public final ID_TYPE id;

    @Schema(description = "Latest measurement time", requiredMode = Schema.RequiredMode.REQUIRED)
    public final Instant measuredTime;

    public StationMeasurementDataV1(final ID_TYPE id, final Instant measuredTime) {
        this.id = id;
        this.measuredTime = measuredTime;
    }
}