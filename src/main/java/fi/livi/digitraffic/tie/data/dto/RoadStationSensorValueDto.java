package fi.livi.digitraffic.tie.data.dto;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@ApiModel(value = "RoadStationSensorValue", description = "Road wather station sensor value")
@JsonPropertyOrder(value = { "sensorName", "sensorId", "sensorValue", "sensorUnit", "sensorValueMeasuredLocalTime", "conditionUpdatedUtc"})
public class RoadStationSensorValueDto {

    @Id
    @JsonIgnore
    private long sensorValueId;

    @ApiModelProperty(value = "Measured sensor value", required = true, position = 3)
    private double sensorValue;

    @ApiModelProperty(value = "Measured sensor value unit", required = true, position = 4)
    private String sensorUnit;

    @JsonIgnore
    @ApiModelProperty(value = "Sensor value measurement time", required = true)
    private LocalDateTime sensorValueMeasured;

    @JsonIgnore
    private long roadStationNaturalId;

    @JsonIgnore
    private long roadStationId;

    @ApiModelProperty(value = "Sensor type id (naturalId)", required = true, position = 2)
    @JsonProperty("sensorId")
    private long sensorNaturalId;

    @JsonIgnore
    private long sensorId;

    @ApiModelProperty(value = "Sensor name", position = 1, required = true)
    private String sensorName;

    public long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    public void setRoadStationNaturalId(long roadStationNaturalId) {
        this.roadStationNaturalId = roadStationNaturalId;
    }

    public long getRoadStationId() {
        return roadStationId;
    }

    public void setRoadStationId(long roadStationId) {
        this.roadStationId = roadStationId;
    }

    public long getSensorNaturalId() {
        return sensorNaturalId;
    }

    public void setSensorNaturalId(long sensorNaturalId) {
        this.sensorNaturalId = sensorNaturalId;
    }

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(long sensorId) {
        this.sensorId = sensorId;
    }

    public long getSensorValueId() {
        return sensorValueId;
    }

    public void setSensorValueId(long sensorValueId) {
        this.sensorValueId = sensorValueId;
    }

    public double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(double sensorValue) {
        this.sensorValue = sensorValue;
    }

    public LocalDateTime getSensorValueMeasured() {
        return sensorValueMeasured;
    }

    public void setSensorValueMeasured(LocalDateTime sensorValueMeasured) {
        this.sensorValueMeasured = sensorValueMeasured;
    }

    @ApiModelProperty(value = "Sensor value measurement " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE, required = true, position = 5)
    public String getSensorValueMeasuredLocalTime() {
        return ToStringHelpper.toString(sensorValueMeasured, ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Sensor value measurement " + ToStringHelpper.ISO_8601_UTC_TIMESTAMP_EXAMPLE, required = true, position = 6)
    public String getSensorValueMeasuredUtc() {
        return ToStringHelpper.toString(sensorValueMeasured, ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorUnit() {
        return sensorUnit;
    }

    public void setSensorUnit(String sensorUnit) {
        this.sensorUnit = sensorUnit;
    }
}
