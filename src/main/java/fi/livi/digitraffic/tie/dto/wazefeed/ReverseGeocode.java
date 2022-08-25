package fi.livi.digitraffic.tie.dto.wazefeed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReverseGeocode {
    @JsonProperty("lat")
    public final double latitude;

    @JsonProperty("lon")
    public final double longitude;

    @JsonProperty("radius")
    public final int radius;

    @JsonProperty("result")
    public final List<ReverseGeocodeResult> results;

    public ReverseGeocode(final double latitude, final double longitude, final int radius, final List<ReverseGeocodeResult> results) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.results = results;
    }
}