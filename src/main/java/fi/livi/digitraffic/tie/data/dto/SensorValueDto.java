package fi.livi.digitraffic.tie.data.dto;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Immutable
@ApiModel(value = "SensorValue", description = "Sensor value")
@JsonPropertyOrder(value = { "id", "roadStationId", "name", "oldName", "shortName", "sensorValueId", "sensorValue", "sensorUnit"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorValueDto {

    @Id
    @JsonIgnore
    private long sensorValueId;

    @ApiModelProperty(value = "Measured sensor value", required = true, position = 3)
    private double sensorValue;

    @ApiModelProperty(value = "Measured sensor value unit", required = true, position = 4)
    private String sensorUnit;

    @JsonProperty("roadStationId")
    private long roadStationNaturalId;

    @JsonIgnore
    private long roadStationId;

    @ApiModelProperty(value = "Sensor type id (naturalId)", required = true, position = 2)
    @JsonProperty(value = "id")
    private long sensorNaturalId;

    @JsonIgnore
    private long sensorId;

    /** @deprecated */
    @Deprecated
    @ApiModelProperty(value = "Sensor old name. For new sensors will equal name. Will deprecate in future version.", position = 1, notes = "noteja")
    @JsonProperty("oldName")
    private String sensorNameOld;

    @ApiModelProperty(value = "Sensor name", position = 1, required = true)
    @JsonProperty(value = "name")
    private String sensorNameFi;

    @ApiModelProperty(value = "Sensor short name", position = 2, required = true)
    @JsonProperty(value = "shortName")
    private String sensorShortNameFi;

    @ApiModelProperty(value = "Additional information of sensor value [fi]")
    private String sensorValueDescriptionFi;

    @ApiModelProperty(value = "Additional information of sensor value [en]")
    private String sensorValueDescriptionEn;

    @JsonIgnore
    private ZonedDateTime measuredTime;

    @JsonIgnore
    private ZonedDateTime stationLatestMeasuredTime;

    /** Db's timestamp */
    @JsonIgnore
    private ZonedDateTime updatedTime;

    public long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    public void setRoadStationNaturalId(final long roadStationNaturalId) {
        this.roadStationNaturalId = roadStationNaturalId;
    }

    public long getRoadStationId() {
        return roadStationId;
    }

    public void setRoadStationId(final long roadStationId) {
        this.roadStationId = roadStationId;
    }

    public long getSensorNaturalId() {
        return sensorNaturalId;
    }

    public void setSensorNaturalId(final long sensorNaturalId) {
        this.sensorNaturalId = sensorNaturalId;
    }

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(final long sensorId) {
        this.sensorId = sensorId;
    }

    public long getSensorValueId() {
        return sensorValueId;
    }

    public void setSensorValueId(final long sensorValueId) {
        this.sensorValueId = sensorValueId;
    }

    public double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(final double sensorValue) {
        this.sensorValue = sensorValue;
    }

    public String getSensorNameOld() {
        return sensorNameOld;
    }

    public void setSensorNameOld(final String sensorNameOld) {
        this.sensorNameOld = sensorNameOld;
    }

    public String getSensorUnit() {
        return sensorUnit;
    }

    public void setSensorUnit(final String sensorUnit) {
        this.sensorUnit = sensorUnit;
    }

    public String getSensorValueDescriptionFi() {
        return sensorValueDescriptionFi;
    }

    public void setSensorValueDescriptionFi(final String sensorValueDescriptionFi) {
        this.sensorValueDescriptionFi = sensorValueDescriptionFi;
    }

    public String getSensorValueDescriptionEn() {
        return sensorValueDescriptionEn;
    }

    public void setSensorValueDescriptionEn(final String sensorValueDescriptionEn) {
        this.sensorValueDescriptionEn = sensorValueDescriptionEn;
    }

    public String getSensorNameFi() {
        return sensorNameFi;
    }

    public void setSensorNameFi(final String sensorNameFi) {
        this.sensorNameFi = sensorNameFi;
    }

    public String getSensorShortNameFi() {
        return sensorShortNameFi;
    }

    public void setSensorShortNameFi(final String sensorShortNameFi) {
        this.sensorShortNameFi = sensorShortNameFi;
    }

    public ZonedDateTime getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(final ZonedDateTime measuredTime) {
        this.measuredTime = measuredTime;
    }

    public ZonedDateTime getStationLatestMeasuredTime() {
        return stationLatestMeasuredTime;
    }

    public void setStationLatestMeasuredTime(final ZonedDateTime stationLatestMeasured) {
        this.stationLatestMeasuredTime = stationLatestMeasured;
    }

    public static ZonedDateTime getStationLatestMeasurement(final List<SensorValueDto> sensorValues) {
        if (sensorValues != null && !sensorValues.isEmpty()) {
            return sensorValues.get(0).getStationLatestMeasuredTime();
        }
        return null;
    }

    public ZonedDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(ZonedDateTime updated) {
        this.updatedTime = updated;
    }
}
