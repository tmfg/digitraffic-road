package fi.livi.digitraffic.tie.dto.weather.forecast.v1;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = RoadSegmentDtoV1.API_DESCRIPTION)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoadSegmentDtoV1 {

    public static final String API_DESCRIPTION = "Forecast section road segments. Refers to https://aineistot.vayla.fi/digiroad/";

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
