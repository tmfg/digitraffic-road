package fi.livi.digitraffic.tie.metadata.model;

import java.util.List;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The message contains road stations' data collection and sensor calculation statuses information for all road station types. "
                      + "In addition, the message contains road station condition status for all station types except the LAM type.", parent = RootDataObjectDto.class)
public class RoadStationStatusesData extends RootDataObjectDto {

    @ApiModelProperty(value = "Road station statuses'",required = true)
    private final List<RoadStationStatus> roadStationStatuses;

    public RoadStationStatusesData(final List<RoadStationStatus> roadStationStatuses) {
        this.roadStationStatuses = roadStationStatuses;
    }

    public List<RoadStationStatus> getRoadStationStatuses() {
        return roadStationStatuses;
    }

    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("localTime", getDataLocalTime())
                .appendField("dataUtc", getDataUtc())
                .appendField("roadStationStatuses", getRoadStationStatuses())
                .toString();
    }
}
