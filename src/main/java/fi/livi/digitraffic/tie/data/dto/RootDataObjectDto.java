package fi.livi.digitraffic.tie.data.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;

@JsonPropertyOrder({"dataLocalTime", "dataUtc"})
public class RootDataObjectDto {

    @JsonIgnore
    private final ZonedDateTime timestamp;

    public RootDataObjectDto(final ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public RootDataObjectDto() {
        this.timestamp = ZonedDateTime.now();
    }

    public RootDataObjectDto(LocalDateTime localTimestamp) {
        if (localTimestamp != null) {
            timestamp = localTimestamp.atZone(ZoneId.systemDefault());
        } else {
            timestamp = null;
        }
    }

    @ApiModelProperty(value = "Data read " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE, required = true)
    public String getDataLocalTime() {
        return ToStringHelpper.toString(timestamp, ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Data read " + ToStringHelpper.ISO_8601_UTC_TIMESTAMP_EXAMPLE, required = true)
    public String getDataUtc() {
        return ToStringHelpper.toString(timestamp, ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }
}
