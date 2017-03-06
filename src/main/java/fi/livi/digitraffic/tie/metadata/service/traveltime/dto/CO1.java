package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CO1 {

    public final String y;

    public final String x;

    public CO1(@JsonProperty("Y") final String y,
               @JsonProperty("X") final String x) {
        this.y = y;
        this.x = x;
    }
}
