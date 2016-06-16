package fi.livi.digitraffic.tie.data.dto.trafficfluency;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "TrafficFluencyData", description = "The latest 5 minute median, corresponding average speed, fluency class, and timestamp of the latest update for each link", parent = RootDataObjectDto.class)
@JsonPropertyOrder({ "dataUptadedLocalTime", "dataUptadedUtc", "latestMedians" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficFluencyRootDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Latest medians")
    private final List<LatestMedianDataDto> latestMedians;

    public TrafficFluencyRootDataObjectDto(final List<LatestMedianDataDto> latestMedians,
                                           LocalDateTime uptaded) {
        super(uptaded);
        this.latestMedians = latestMedians;
    }

    public List<LatestMedianDataDto> getLatestMedians() {
        return latestMedians;
    }
}
