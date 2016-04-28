package fi.livi.digitraffic.tie.data.model.trafficfluency;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.data.model.DataObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The latest 5 minute median, corresponding average speed, fluency class, and timestamp of the latest update for each link")
@JsonPropertyOrder({ "dataLocalTime", "dataUtc", "latestMedianData" })
public class TrafficFluencyDataObject extends DataObject {

    @ApiModelProperty(value = "", required = true)
    private final List<LatestMedianData> latestMedianData;

    public TrafficFluencyDataObject(final List<LatestMedianData> latestMedianData) {
        this.latestMedianData = latestMedianData;
    }

    public List<LatestMedianData> getLatestMedianData() {
        return latestMedianData;
    }
}
