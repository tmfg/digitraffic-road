package fi.livi.digitraffic.tie.metadata.model;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;

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

    @ApiModelProperty(value = "Timestamp in ISO 8601 format with time offsets from UTC (eg. 2016-04-20T12:38:16.328+03:00)", required = true)
    public String getLocalTime() {
        return timestamp.toOffsetDateTime().toString();
    }

    @ApiModelProperty(value = "Timestamp in ISO 8601 UTC format (eg. 2016-04-20T09:38:16.328Z)", required = true)
    public String getUtc() {
        return ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneOffset.UTC).toString();
    }
}
