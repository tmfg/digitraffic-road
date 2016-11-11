package fi.livi.digitraffic.tie.data.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.helper.ToStringHelpper;
import io.swagger.annotations.ApiModelProperty;

@Immutable
@JsonPropertyOrder({"dataUpdatedLocalTime", "dataUpdatedUtc"})
public class RootDataObjectDto {

    @JsonIgnore
    private final ZonedDateTime timestamp;

    public RootDataObjectDto(final ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public RootDataObjectDto(final LocalDateTime localTimestamp) {
        if (localTimestamp != null) {
            timestamp = localTimestamp.atZone(ZoneId.systemDefault());
        } else {
            timestamp = null;
        }
    }

    @ApiModelProperty(value = "Data last updated " + ToStringHelpper.ISO_8601_OFFSET_TIMESTAMP_EXAMPLE, required = true)
    public String getDataUpdatedLocalTime() {
        return ToStringHelpper.toString(timestamp, ToStringHelpper.TimestampFormat.ISO_8601_WITH_ZONE_OFFSET);
    }

    @ApiModelProperty(value = "Data last updated " + ToStringHelpper.ISO_8601_UTC_TIMESTAMP_EXAMPLE, required = true)
    public String getDataUpdatedUtc() {
        return ToStringHelpper.toString(timestamp, ToStringHelpper.TimestampFormat.ISO_8601_UTC);
    }
}
