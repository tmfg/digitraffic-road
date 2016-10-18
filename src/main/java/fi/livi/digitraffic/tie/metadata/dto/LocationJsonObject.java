package fi.livi.digitraffic.tie.metadata.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationJsonObject {
    @ApiModelProperty(value = "Unique number for each object in the database", required = true)
    public final int locationCode;

    @ApiModelProperty(value = "Code of location subtype", required = true)
    public final String subtypeCode;

    @ApiModelProperty("Roadnumber for roads. Junctionno: the numbering of exits has only just begun on the very limited Finnish motorway network. The exit numbers will be included. NOTE: the roads, segments and points are not sorted in ascending order")
    public final String roadJunction;

    @ApiModelProperty("Roadname if exists, L5.0 always have road name")
    public final String roadName;

    @ApiModelProperty(value = "For roads and segments this is the name of the starting point. For all other objects (linear (streets), area and point) this is the name of the object", required = true)
    public final String firstName;
    @ApiModelProperty("For roads and segments this is the name of the ending point. For point locations the number of the intersecting road")
    public final String secondName;

    @ApiModelProperty("Code of the upper order administrative area")
    public final Integer areaRef;

    @ApiModelProperty("For segments and point locations. Describes the code of the segment which these objects belong to. If there are no segments on the road the location code of the road is given instead")
    public final Integer linearRef;

    @ApiModelProperty("For segments and point locations. Segments: describes the code of previous segment on that road. For the first segment on the road this code is 0. Points: describes the code of previous point on that road. For the starting point this code is 0")
    public final Integer negOffset;
    @ApiModelProperty("For segments and point locations. Segments: describes the code of next segment on that road. For the last segment on the road this code is 0. Points: describes the code of next point on that road. For the last point this code is 0")
    public final Integer posOffset;

    @ApiModelProperty("Indicates whether a point is within the city limits (1) or not (0). NOTE: Not actively entered yet")
    public final Boolean urban;

    @ApiModelProperty("Coordinate in WGS84, for all points")
    public final BigDecimal wsg84Lat;
    @ApiModelProperty("Coordinate in WGS84, for all points")
    public final BigDecimal wsg84Long;

    @ApiModelProperty("For all L5.0 and for some roads. Text to be used when the incident has an effect only on vehicles driving in the negative direction of the road. ( e.g. Ring 1 westbound)")
    public final String negDirection;
    @ApiModelProperty("For all L5.0 and for some roads. Text to be used when the incident has an effect only on vehicles driving in the positive direction of the road. ( e.g. Ring 1 eastbound)")
    public final String posDirection;

    public LocationJsonObject(final int locationCode, final String subtypeCode, final String roadJunction, final String roadName, final String firstName, final String secondName,
                              final Integer areaRef, final Integer linearRef,
                              final Integer negOffset, final Integer posOffset, final Boolean urban, final BigDecimal wsg84Lat, final BigDecimal wsg84Long,
                              final String negDirection, final String posDirection) {
        this.locationCode = locationCode;
        this.subtypeCode = subtypeCode;
        this.roadJunction = roadJunction;
        this.roadName = roadName;
        this.firstName = firstName;
        this.secondName = secondName;
        this.areaRef = areaRef;
        this.linearRef = linearRef;
        this.negOffset = negOffset;
        this.posOffset = posOffset;
        this.urban = urban;
        this.wsg84Lat = wsg84Lat;
        this.wsg84Long = wsg84Long;
        this.negDirection = negDirection;
        this.posDirection = posDirection;
    }
}
