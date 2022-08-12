package fi.livi.digitraffic.tie.dto.wazefeed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReverseGeocodeResult {
    @JsonProperty("distance")
    public final double distance;

    @JsonProperty("names")
    public final List<String> names;

    public ReverseGeocodeResult(double distance, List<String> names) {
        this.distance = distance;
        this.names = names;
    }
}