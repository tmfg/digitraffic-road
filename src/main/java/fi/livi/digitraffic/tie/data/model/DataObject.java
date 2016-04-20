package fi.livi.digitraffic.tie.data.model;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({"localTime", "utc"})
public class DataObject {

    private final ZonedDateTime timestamp;

    public DataObject(final ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public DataObject() {
        this.timestamp = ZonedDateTime.now();
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
