package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Timestamp {

    public final String localtime;

    public final String utc;

    public Timestamp(@JsonProperty("localtime") final String localtime,
                     @JsonProperty("utc") final String utc) {
        this.localtime = localtime;
        this.utc = utc;
    }
}
