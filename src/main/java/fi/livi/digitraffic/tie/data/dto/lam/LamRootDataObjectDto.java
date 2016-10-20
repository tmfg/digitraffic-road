package fi.livi.digitraffic.tie.data.dto.lam;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TmsData", description = "Latest measurement data from TMS Stations", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUpdatedLocalTime", "dataUpdatedUtc", "lamStations"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LamRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "TMS Stations data")
    @JsonProperty(value = "tmsStations")
    private final List<LamStationDto> lamStations;

    public LamRootDataObjectDto(final List<LamStationDto> lamStations, final LocalDateTime updated) {
        super(updated);
        this.lamStations = lamStations;
    }

    public LamRootDataObjectDto(final LocalDateTime updated) {
        this(null, updated);
    }

    public List<LamStationDto> getLamStations() {
        return lamStations;
    }

}

