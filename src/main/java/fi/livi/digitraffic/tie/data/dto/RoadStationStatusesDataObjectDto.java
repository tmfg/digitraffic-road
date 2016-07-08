package fi.livi.digitraffic.tie.data.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import fi.livi.digitraffic.tie.metadata.model.RoadStationStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "RoadStationStatusesData",
          description = "The message contains road stations' data collection and sensor calculation statuses information for all road station types. "
                      + "In addition, the message contains road station condition status for all station types except the LAM type.", parent = RootDataObjectDto.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadStationStatusesDataObjectDto extends RootDataObjectDto {

    @ApiModelProperty(value = "Road station statuses'")
    private final List<RoadStationStatus> roadStationStatuses;

    public RoadStationStatusesDataObjectDto(final List<RoadStationStatus> roadStationStatuses, LocalDateTime lastUpdated) {
        super(lastUpdated);
        this.roadStationStatuses = roadStationStatuses;
    }

    public RoadStationStatusesDataObjectDto(LocalDateTime updated) {
        this(null, updated);
    }

    public List<RoadStationStatus> getRoadStationStatuses() {
        return roadStationStatuses;
    }

    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("localTime", getDataUptadedLocalTime())
                .appendField("dataUtc", getDataUptadedUtc())
                .appendField("roadStationStatuses", getRoadStationStatuses())
                .toString();
    }
}
