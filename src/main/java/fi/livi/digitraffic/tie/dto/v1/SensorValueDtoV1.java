package fi.livi.digitraffic.tie.dto.v1;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.*;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.model.roadstation.SensorValueReliability;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;

@Schema(description = "Sensor value")
@JsonPropertyOrder({"id", "stationId", "name", "oldName", "shortName", "sensorValueId", "sensorValue", "sensorUnit", "timeWindowStart", "timeWindowEnd", "measuredTime"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface SensorValueDtoV1 {

    @Id
    @JsonIgnore
    Long getSensorValueId();

    @Schema(description = "Measured sensor value", requiredMode = REQUIRED)
    double getValue();

    @Schema(description = "Measured sensor value unit", requiredMode = REQUIRED)
    String getUnit();

    @Schema(description = "Id of the road station", requiredMode = REQUIRED)
    @JsonProperty(value = "stationId", required = true)
    long getRoadStationNaturalId();

    @Schema(description = "Sensor type id (naturalId)", requiredMode = REQUIRED)
    @JsonProperty(value = "id")
    long getSensorNaturalId();

    @Schema(description = "Sensor name", requiredMode = REQUIRED)
    @JsonProperty(value = "name")
    String getSensorNameFi();

    @Schema(description = "Sensor short name", requiredMode = REQUIRED)
    @JsonProperty(value = "shortName")
    String getSensorShortNameFi();

    @Schema(description = "Additional information of sensor value [fi]")
    String getSensorValueDescriptionFi();

    @Schema(description = "Additional information of sensor value [en]")
    String getSensorValueDescriptionEn();

    @JsonIgnore
    Instant getStationLatestMeasuredTime();

    @JsonIgnore
    Instant getStationLatestModifiedTime();

    @Schema(description = "Measurement time window start time (only for fixed time window sensors)")
    Instant getTimeWindowStart();

    @Schema(description = "Measurement time window end time (only for fixed time window sensors)")
    Instant getTimeWindowEnd();

    /** Db's timestamp */
    @JsonIgnore
    Instant getModified();

    @Schema(description = "Measurement time", requiredMode = REQUIRED)
    Instant getMeasuredTime();

    @Schema(description = "Measurement reliability information", requiredMode = NOT_REQUIRED)
    SensorValueReliability getReliability();


    static Instant getStationLatestMeasurement(final List<SensorValueDtoV1> sensorValues) {
        if (sensorValues != null && !sensorValues.isEmpty()) {
            return sensorValues.getFirst().getStationLatestMeasuredTime();
        }
        return Instant.EPOCH;
    }

    static Instant getStationLatestUpdated(final List<SensorValueDtoV1> sensorValues) {
        if (sensorValues != null && !sensorValues.isEmpty()) {
            return sensorValues.getFirst().getStationLatestModifiedTime();
        }
        return Instant.EPOCH;
    }
}
