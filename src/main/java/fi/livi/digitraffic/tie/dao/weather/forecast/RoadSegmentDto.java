package fi.livi.digitraffic.tie.dao.weather.forecast;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoadSegmentDto {

    public final Integer startDistance;
    public final Integer endDistance;
    public final Integer carriageway;

    public RoadSegmentDto(
        @JsonProperty("startDistance")
        final Integer startDistance,
        @JsonProperty("endDistance")
        final Integer endDistance,
        @JsonProperty("carriageway")
        final Integer carriageway) {
        this.startDistance = startDistance;
        this.endDistance = endDistance;
        this.carriageway = carriageway;
    }
}
