package fi.livi.digitraffic.tie.data.dto.lam;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "LamStationData", description = "Lam Station with sensor values", parent = AbstractStationWithSensorsDto.class)
@JsonPropertyOrder( value = {"id", "lamNumber", "measuredLocalTime", "measuredUtc", "sensorValues"})
public class LamStationDto extends AbstractStationWithSensorsDto {

    @ApiModelProperty(value = "Lam number", required = true)
    @JsonProperty(value = "lamNumber")
    private long lamStationNaturalId;

    public long getLamStationNaturalId() {
        return lamStationNaturalId;
    }

    public void setLamStationNaturalId(long lamStationNaturalId) {
        this.lamStationNaturalId = lamStationNaturalId;
    }
}
