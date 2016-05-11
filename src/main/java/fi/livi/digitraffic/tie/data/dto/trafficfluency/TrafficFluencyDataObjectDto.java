package fi.livi.digitraffic.tie.data.dto.trafficfluency;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.dto.DataObjectDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The latest 5 minute median, corresponding average speed, fluency class, and timestamp of the latest update for each link")
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "latestMedianData" })
public class TrafficFluencyDataObjectDto extends DataObjectDto {

    @ApiModelProperty(value = "", required = true)
    private final List<LatestMedianDataDto> latestMedianData;

    public TrafficFluencyDataObjectDto(final List<LatestMedianDataDto> latestMedianData) {
        this.latestMedianData = latestMedianData;
    }

    public List<LatestMedianDataDto> getLatestMedianData() {
        return latestMedianData;
    }
}
