package fi.livi.digitraffic.tie.dto.weather.v1.forecast;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadSegmentDtoV1 {


    @Schema(description = "Road segment start distance")
    public final Integer startDistance;

    @Schema(description = "Road segment end distance")
    public final Integer endDistance;

    @Schema(description = "Road segment carriageway")
    public final Integer carriageway;

    public RoadSegmentDtoV1(final Integer startDistance, final Integer endDistance,
                            final Integer carriageway) {
        this.startDistance = startDistance;
        this.endDistance = endDistance;
        this.carriageway = carriageway;
    }
}
