package fi.livi.digitraffic.tie.metadata.service.traveltime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WGS84 {

    public final String lon;

    public final String lat;

    public WGS84(@JsonProperty("lon") final String lon,
                 @JsonProperty("lat") final String lat) {
        this.lon = lon;
        this.lat = lat;
    }
}
