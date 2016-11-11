package fi.livi.digitraffic.tie.metadata.dto;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "locationCode", "subtypeCode", "firstName", "secondName" })
public interface LocationJson {
    @ApiModelProperty(value = "Unique number for each object in the database", required = true)
    int getLocationCode();

    @ApiModelProperty(value = "Code of location subtype", required = true)
    @Value("#{target.locationSubtype.subtypeCode}")
    String getSubtypeCode();

    @ApiModelProperty("Roadnumber for roads. Junctionno: the numbering of exits has only just begun on the very limited Finnish motorway network. The exit numbers will be included. NOTE: the roads, segments and points are not sorted in ascending order")
    String getRoadJunction();

    @ApiModelProperty("Roadname if exists, L5.0 always have road name")
    String getRoadName();

    @ApiModelProperty(value = "For roads and segments this is the name of the starting point. For all other objects (linear (streets), area and point) this is the name of the object", required = true)
    String getFirstName();

    @ApiModelProperty("For roads and segments this is the name of the ending point. For point locations the number of the intersecting road")
    String getSecondName();

    @ApiModelProperty("Code of the upper order administrative area")
    @Value("#{target.areaRef == null ? null : target.areaRef.locationCode}")
    Integer getAreaRef();

    @ApiModelProperty("For segments and point locations. Describes the code of the segment which these objects belong to. If there are no segments on the road the location code of the road is given instead")
    @Value("#{target.linearRef == null ? null : target.linearRef.locationCode}")
    Integer getLinearRef();

    @ApiModelProperty("For segments and point locations. Segments: describes the code of previous segment on that road. For the first segment on the road this code is 0. Points: describes the code of previous point on that road. For the starting point this code is 0")
    Integer getNegOffset();

    @ApiModelProperty("For segments and point locations. Segments: describes the code of next segment on that road. For the last segment on the road this code is 0. Points: describes the code of next point on that road. For the last point this code is 0")
    Integer getPosOffset();

    @ApiModelProperty("Indicates whether a point is within the city limits (1) or not (0). NOTE: Not actively entered yet")
    Boolean getUrban();

    @ApiModelProperty("Coordinate in WGS84, for all points")
    BigDecimal getWgs84Lat();

    @ApiModelProperty("Coordinate in WGS84, for all points")
    BigDecimal getWgs84Long();

    @ApiModelProperty("For all L5.0 and for some roads. Text to be used when the incident has an effect only on vehicles driving in the negative direction of the road. ( e.g. Ring 1 westbound)")
    String getNegDirection();

    @ApiModelProperty("For all L5.0 and for some roads. Text to be used when the incident has an effect only on vehicles driving in the positive direction of the road. ( e.g. Ring 1 eastbound)")
    String getPosDirection();

    @ApiModelProperty("Point location according to Finnish Transport Agency’s official addressing where Locations on road network are addressed as: Road number;Road part number;Carriageway; Distance from the beginning of the road part")
    String getGeocode();

    @ApiModelProperty("The order of point within line or segment feature")
    String getOrderOfPoint();
}
