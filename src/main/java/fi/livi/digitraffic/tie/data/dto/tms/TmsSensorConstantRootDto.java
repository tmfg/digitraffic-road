package fi.livi.digitraffic.tie.data.dto.tms;

import java.time.ZonedDateTime;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@ApiModel(value = "TmsSensorConstantData", description = "Latest constant values TMS Stations", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUpdatedTime", "sensorConstants"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmsSensorConstantRootDto extends RootDataObjectDto {

    @ApiModelProperty(value = "TMS Stations sensor constants data", required = true)
    @JsonProperty(value = "tmsStations")
    private List<TmsSensorConstantDto> sensorConstantDtos;

    public TmsSensorConstantRootDto(ZonedDateTime dataUpdatedTime, final List<TmsSensorConstantDto> sensorConstantDtos) {
        super(dataUpdatedTime);
        this.sensorConstantDtos = sensorConstantDtos;
    }

    public List<TmsSensorConstantDto> getSensorConstantDtos() {
        return sensorConstantDtos;
    }
}
