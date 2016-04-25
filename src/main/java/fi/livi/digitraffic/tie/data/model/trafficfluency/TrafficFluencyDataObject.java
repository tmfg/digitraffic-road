package fi.livi.digitraffic.tie.data.model.trafficfluency;

import java.util.List;

import fi.livi.digitraffic.tie.data.model.DataObject;
import io.swagger.annotations.ApiModelProperty;

public class TrafficFluencyDataObject extends DataObject {

    @ApiModelProperty(value = "", required = true)
    private final List<LatestMedianData> latestMedianDatas;

    public TrafficFluencyDataObject(final List<LatestMedianData> latestMedianDatas) {
        this.latestMedianDatas = latestMedianDatas;
    }

    public List<LatestMedianData> getLatestMedianDatas() {
        return latestMedianDatas;
    }
}
