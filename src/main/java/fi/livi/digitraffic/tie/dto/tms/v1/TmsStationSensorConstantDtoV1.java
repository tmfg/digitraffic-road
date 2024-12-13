package fi.livi.digitraffic.tie.dto.tms.v1;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationDataV1;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantValueDtoV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sensor constant of TMS Station")
@JsonPropertyOrder({ "id", "dataUpdatedTime" })
public class TmsStationSensorConstantDtoV1 extends StationDataV1<Long> {

    @Schema(description = "TMS Stations sensor constant values", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(value = "sensorConstantValues")
    public final List<TmsSensorConstantValueDtoV1> sensorConstanValues;

    public TmsStationSensorConstantDtoV1(final Long roadStationNaturalId, final List<TmsSensorConstantValueDtoV1> sensorConstanValues) {
        super(roadStationNaturalId, findLatestUpdatedTime(sensorConstanValues));
        this.sensorConstanValues = sensorConstanValues;
    }

    private static Instant findLatestUpdatedTime(final List<TmsSensorConstantValueDtoV1> sensorConstanValues) {
        if (sensorConstanValues == null || sensorConstanValues.isEmpty()) {
            return null;
        }
        return sensorConstanValues.stream()
            .map(TmsSensorConstantValueDtoV1::getModified)
            .max(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public boolean shouldContainLastModified() {
        return sensorConstanValues != null && !sensorConstanValues.isEmpty();
    }
}
