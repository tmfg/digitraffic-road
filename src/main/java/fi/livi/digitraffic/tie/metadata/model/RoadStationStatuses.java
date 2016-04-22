package fi.livi.digitraffic.tie.metadata.model;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The message contains road stations' data collection and sensor calculation statuses information for all road station types. "
                      + "In addition, the message contains road station condition status for all station types except the LAM type.")
public class RoadStationStatuses {

    @JsonIgnore
    private final ZonedDateTime timestamp;

    private final List<RoadStationStatus> roadStationStatusData;

    public RoadStationStatuses(final List<RoadStationStatus> roadStationStatusData) {
        this.timestamp = ZonedDateTime.now();
        this.roadStationStatusData = roadStationStatusData;
    }

    public String getTimestamp() {
        return timestamp.toString();
    }

    public List<RoadStationStatus> getRoadStationStatusData() {
        return roadStationStatusData;
    }

    @ApiModelProperty(value = "Latest update timestamp of the message in ISO 8601 format with time offsets from UTC (eg. 2016-04-20T12:38:16.328+03:00)", required = true)
    public String getLocalTime() {
        return timestamp.toOffsetDateTime().toString();
    }

    @ApiModelProperty(value = "Latest update timestamp of the message in ISO 8601 UTC format (eg. 2016-04-20T09:38:16.328Z)", required = true)
    public String getUtc() {
        return ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC).toString();
    }

    @Override
    public String toString() {
        return new ToStringHelpper(this)
                .appendField("localTime", getLocalTime())
                .appendField("getUtc", getUtc())
                .appendField("roadStationStatusData", getRoadStationStatusData())
                .toString();
    }
}
