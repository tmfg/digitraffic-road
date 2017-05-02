package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimestampDto {

    public final ZonedDateTime localtime;

    public final ZonedDateTime utc;

    public TimestampDto(@JsonProperty("localtime") final ZonedDateTime localtime,
                        @JsonProperty("utc") final ZonedDateTime utc) {
        this.localtime = localtime;
        this.utc = utc;
    }
}
