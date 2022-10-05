package fi.livi.digitraffic.tie.dto.v1;

import java.time.Instant;
import java.util.List;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sensor value")
@JsonPropertyOrder({"id", "stationId", "name", "oldName", "shortName", "sensorValueId", "sensorValue", "sensorUnit", "timeWindowStart", "timeWindowEnd", "measuredTime"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface SensorValueDtoV1 {

    @Id
    @JsonIgnore
    Long getSensorValueId();

    @Schema(description = "Measured sensor value", required = true)
    double getValue();

    @Schema(description = "Measured sensor value unit", required = true)
    String getUnit();

    @JsonProperty(value = "stationId", required = true)
    long getRoadStationNaturalId();

    @Schema(description = "Sensor type id (naturalId)", required = true)
    @JsonProperty(value = "id")
    long getSensorNaturalId();

    @Schema(description = "Sensor name", required = true)
    @JsonProperty(value = "name")
    String getSensorNameFi();

    @Schema(description = "Sensor short name", required = true)
    @JsonProperty(value = "shortName")
    String getSensorShortNameFi();

    @Schema(description = "Additional information of sensor value [fi]")
    String getSensorValueDescriptionFi();

    @Schema(description = "Additional information of sensor value [en]")
    String getSensorValueDescriptionEn();

    @JsonIgnore
    Instant getStationLatestMeasuredTime();

    @Schema(description = "Measurement time window start time (only for fixed time window sensors)")
    Instant getTimeWindowStart();

    @Schema(description = "Measurement time window end time (only for fixed time window sensors)")
    Instant getTimeWindowEnd();

    /** Db's timestamp */
    @JsonIgnore
    Instant getUpdatedTime();

    @Schema(description = "Measurement time", required = true)
    Instant getMeasuredTime();


    static Instant getStationLatestMeasurement(final List<SensorValueDtoV1> sensorValues) {
        if (sensorValues != null && !sensorValues.isEmpty()) {
            return sensorValues.get(0).getStationLatestMeasuredTime();
        }
        return Instant.EPOCH;
    }
}
