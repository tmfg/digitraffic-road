package fi.livi.digitraffic.tie.data.dto.lam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.SensorValueDto;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "LamStationData", description = "Lam Station with sensor values")
@JsonPropertyOrder( value = {"id", "lamNumber", "measuredLocalTime", "measuredUtc", "sensorValues"})
public class LamStationDto {

    @ApiModelProperty(value = "Road station id", required = true)
    @JsonProperty(value = "id")
    private long roadStationNaturalId;

    @ApiModelProperty(value = "Lam number", required = true)
    @JsonProperty(value = "lamNumber")
    private long lamStationNaturalId;

    @ApiModelProperty(value = "Measured sensor values of the Weather Station", required = true)
    private List<SensorValueDto> sensorValues = new ArrayList<>();

    @JsonIgnore
    private LocalDateTime measured;

    public long getRoadStationNaturalId() {
        return roadStationNaturalId;
    }

    public void setRoadStationNaturalId(final long roadStationNaturalId) {
        this.roadStationNaturalId = roadStationNaturalId;
    }

    public void addSensorValue(final SensorValueDto sensorValue) {
        sensorValues.add(sensorValue);
    }

    public List<SensorValueDto> getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(final List<SensorValueDto> sensorValues) {
        this.sensorValues = sensorValues;
    }

    public LocalDateTime getMeasured() {
        return measured;
    }

    public void setMeasured(final LocalDateTime measured) {
        this.measured = measured;
    }

    @ApiModelProperty(value = "Values measured " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE)
    public String getMeasuredLocalTime() {
        return ToStringHelpper.toString(getMeasured(), ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Values measured " + ToStringHelpper.ISO_8601_UTC_TIMESTAMP_EXAMPLE)
    public String getMeasuredUtc() {
        return ToStringHelpper.toString(getMeasured(), ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }

    public long getLamStationNaturalId() {
        return lamStationNaturalId;
    }

    public void setLamStationNaturalId(long lamStationNaturalId) {
        this.lamStationNaturalId = lamStationNaturalId;
    }
}
