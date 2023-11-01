package fi.livi.digitraffic.tie.dto.v1;

import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@Immutable
@Schema(name = "SensorValue", description = "Sensor value")
@JsonPropertyOrder({"id", "roadStationId", "name", "oldName", "shortName", "sensorValueId", "sensorValue", "sensorUnit", "timeWindowStart", "timeWindowEnd", "measuredTime"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorValueDto {

    @Id
    @JsonIgnore
    private Long sensorValueId;

    @Schema(description = "Measured sensor value", required = true)
    private double sensorValue;

    @Schema(description = "Measured sensor value unit", required = true)
    private String sensorUnit;

    @JsonProperty("roadStationId")
    private Long roadStationNaturalId;

    @Schema(description = "Sensor type id (naturalId)", required = true)
    @JsonProperty(value = "id")
    private Long sensorNaturalId;

    /** @deprecated */
    @Deprecated
    @Schema(description = "Sensor old name. For new sensors will equal name. Will deprecate in future version.")
    @JsonProperty("oldName")
    private String sensorNameOld;

    @Schema(description = "Sensor name", required = true)
    @JsonProperty(value = "name")
    private String sensorNameFi;

    @Schema(description = "Sensor short name", required = true)
    @JsonProperty(value = "shortName")
    private String sensorShortNameFi;

    @Schema(description = "Additional information of sensor value [fi]")
    private String sensorValueDescriptionFi;

    @Schema(description = "Additional information of sensor value [en]")
    private String sensorValueDescriptionEn;

    @JsonIgnore
    private ZonedDateTime stationLatestMeasuredTime;

    @Schema(description = "Measurement time window start time (only for fixed time window sensors)")
    private ZonedDateTime timeWindowStart;

    @Schema(description = "Measurement time window end time (only for fixed time window sensors)")
    private ZonedDateTime timeWindowEnd;

    /** Db's timestamp */
    @JsonIgnore
    private ZonedDateTime updatedTime;

    @Schema(description = "Measurement time")
    private ZonedDateTime measuredTime;

    public Long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    public void setRoadStationNaturalId(final long roadStationNaturalId) {
        this.roadStationNaturalId = roadStationNaturalId;
    }

    public Long getSensorNaturalId() {
        return sensorNaturalId;
    }

    public void setSensorNaturalId(final Long sensorNaturalId) {
        this.sensorNaturalId = sensorNaturalId;
    }

    public Long getSensorValueId() {
        return sensorValueId;
    }

    public double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValueId(final Long sensorValueId) {
        this.sensorValueId = sensorValueId;
    }

    public String getSensorNameOld() {
        return sensorNameOld;
    }

    public String getSensorUnit() {
        return sensorUnit;
    }

    public String getSensorValueDescriptionFi() {
        return sensorValueDescriptionFi;
    }

    public String getSensorValueDescriptionEn() {
        return sensorValueDescriptionEn;
    }

    public String getSensorNameFi() {
        return sensorNameFi;
    }

    public String getSensorShortNameFi() {
        return sensorShortNameFi;
    }

    public void setSensorShortNameFi(final String sensorShortNameFi) {
        this.sensorShortNameFi = sensorShortNameFi;
    }

    public ZonedDateTime getStationLatestMeasuredTime() {
        return stationLatestMeasuredTime;
    }

    public ZonedDateTime getTimeWindowStart() {
        return timeWindowStart;
    }

    public ZonedDateTime getTimeWindowEnd() {
        return timeWindowEnd;
    }

    public ZonedDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(final ZonedDateTime updated) {
        this.updatedTime = updated;
    }

    public ZonedDateTime getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(final ZonedDateTime measuredTime) {
        this.measuredTime = measuredTime;
    }

    public static ZonedDateTime getStationLatestMeasurement(final List<SensorValueDto> sensorValues) {
        if (sensorValues != null && !sensorValues.isEmpty()) {
            return sensorValues.get(0).getStationLatestMeasuredTime();
        }
        return null;
    }
}
