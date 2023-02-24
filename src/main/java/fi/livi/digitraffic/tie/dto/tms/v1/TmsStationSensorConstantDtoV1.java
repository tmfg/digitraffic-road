package fi.livi.digitraffic.tie.dto.tms.v1;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.roadstation.v1.StationDataV1;
import fi.livi.digitraffic.tie.dto.v1.tms.TmsSensorConstantValueDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sensor constant values of TMS Station")
@JsonPropertyOrder({ "id", "dataUpdatedTime" })
public class TmsStationSensorConstantDtoV1 extends StationDataV1<Long> {

    @Schema(description = "TMS Stations sensor constant values", required = true)
    @JsonProperty(value = "sensorConstantValues")
    public final List<TmsSensorConstantValueDto> sensorConstanValues;

    public TmsStationSensorConstantDtoV1(final Long roadStationNaturalId, final List<TmsSensorConstantValueDto> sensorConstanValues) {
        super(roadStationNaturalId, findLatestUpdatedTime(sensorConstanValues));
        this.sensorConstanValues = sensorConstanValues;
    }

    private static Instant findLatestUpdatedTime(final List<TmsSensorConstantValueDto> sensorConstanValues) {
        if (sensorConstanValues == null || sensorConstanValues.isEmpty()) {
            return null;
        }
        return sensorConstanValues.stream()
            .map(TmsSensorConstantValueDto::getModified)
            .max(Comparator.naturalOrder()).orElse(null);
    }

    @Override
    public boolean shouldContainLastModified() {
        return sensorConstanValues != null && !sensorConstanValues.isEmpty();
    }
}
