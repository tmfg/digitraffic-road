package fi.livi.digitraffic.tie.dto.v1.tms;

import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Immutable
@Schema(name = "TmsSensorConstantV1", description = "Sensor constant values of TMS Station")
@JsonPropertyOrder({ "roadStationId", "sensorConstantValues"})
public class TmsSensorConstantDto { // TODO rename to TmsSensorConstantDtoV1

    @Schema(description = "Id of TMS station", required = true)
    private final Long roadStationId;

    @Schema(description = "TMS Stations sensor constant values", required = true)
    @JsonProperty(value = "sensorConstantValues")
    private List<TmsSensorConstantValueDto> sensorConstantValues;

    public TmsSensorConstantDto(final Long roadStationId, final List<TmsSensorConstantValueDto> sensorConstantValues) {
        this.roadStationId = roadStationId;
        this.sensorConstantValues = sensorConstantValues;
    }

    public Long getRoadStationId() {
        return roadStationId;
    }

    public List<TmsSensorConstantValueDto> getSensorConstantValues() {
        return sensorConstantValues;
    }
}
