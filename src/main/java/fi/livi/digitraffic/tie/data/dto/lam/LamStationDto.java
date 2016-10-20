package fi.livi.digitraffic.tie.data.dto.lam;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TmsStationData", description = "TMS station with sensor values", parent = AbstractStationWithSensorsDto.class)
@JsonPropertyOrder( value = {"id", "tmsNumber", "measuredLocalTime", "measuredUtc", "sensorValues"})
public class LamStationDto extends AbstractStationWithSensorsDto {

    @ApiModelProperty(value = "TMS station number", required = true)
    @JsonProperty(value = "tmsNumber")
    private long lamStationNaturalId;

    public long getLamStationNaturalId() {
        return lamStationNaturalId;
    }

    public void setLamStationNaturalId(long lamStationNaturalId) {
        this.lamStationNaturalId = lamStationNaturalId;
    }
}
