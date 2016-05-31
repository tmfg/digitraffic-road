package fi.livi.digitraffic.tie.data.dto;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@ApiModel(value = "RoadStationSensorValue", description = "Road wather station sensor value")
@JsonPropertyOrder(value = { "sensorNameFi", "sensorNameEn", "sensorShortNameFi", "sensorValueId", "sensorValue", "sensorUnit", "sensorValueMeasuredLocalTime", "conditionUpdatedUtc"})
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @JsonProperty(value = "id")
    private long sensorNaturalId;

    @JsonIgnore
    private long sensorId;

    @ApiModelProperty(value = "Sensor name [en]", position = 1)
    private String sensorNameEn;

    @ApiModelProperty(value = "Sensor name [fi]", position = 1, required = true)
    private String sensorNameFi;

    @ApiModelProperty(value = "Sensor short name [fi]", position = 1, required = true)
    private String sensorShortNameFi;

    @ApiModelProperty(value = "Additional information of sensor value [fi]")
    private String sensorValueDescriptionFi;

    @ApiModelProperty(value = "Additional information of sensor value [en]")
    private String sensorValueDescriptionEn;

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

    public String getSensorNameEn() {
        return sensorNameEn;
    }

    public void setSensorNameEn(String sensorNameEn) {
        this.sensorNameEn = sensorNameEn;
    }

    public String getSensorUnit() {
        return sensorUnit;
    }

    public void setSensorUnit(String sensorUnit) {
        this.sensorUnit = sensorUnit;
    }

    public String getSensorValueDescriptionFi() {
        return sensorValueDescriptionFi;
    }

    public void setSensorValueDescriptionFi(String sensorValueDescriptionFi) {
        this.sensorValueDescriptionFi = sensorValueDescriptionFi;
    }

    public String getSensorValueDescriptionEn() {
        return sensorValueDescriptionEn;
    }

    public void setSensorValueDescriptionEn(String sensorValueDescriptionEn) {
        this.sensorValueDescriptionEn = sensorValueDescriptionEn;
    }

    public String getSensorNameFi() {
        return sensorNameFi;
    }

    public void setSensorNameFi(String sensorNameFi) {
        this.sensorNameFi = sensorNameFi;
    }

    public String getSensorShortNameFi() {
        return sensorShortNameFi;
    }

    public void setSensorShortNameFi(String sensorShortNameFi) {
        this.sensorShortNameFi = sensorShortNameFi;
    }
}
