package fi.livi.digitraffic.tie.data.model;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({"localTime", "utc"})
public class DataObject {

    @JsonIgnore
    private final ZonedDateTime timestamp;

    public DataObject(final ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public DataObject() {
        this.timestamp = ZonedDateTime.now();
    }

    @ApiModelProperty(value = "Timestamp in ISO 8601 format with time offsets from UTC (eg. 2016-04-20T12:38:16.328+03:00)", required = true)
    public String getLocalTime() {
        return ToStringHelpper.toString(timestamp, ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Timestamp in ISO 8601 UTC format (eg. 2016-04-20T09:38:16.328Z)", required = true)
    public String getUtc() {
        return ToStringHelpper.toString(timestamp, ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }
}
