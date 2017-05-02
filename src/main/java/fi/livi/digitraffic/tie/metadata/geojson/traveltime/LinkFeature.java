package fi.livi.digitraffic.tie.metadata.geojson.traveltime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.LineString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Object", value = "LinkFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class LinkFeature {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1)
    @JsonPropertyOrder(value = "1")
    public final String type = "Feature";

    @ApiModelProperty(value = "Link id", required = true, position = 2)
    @JsonPropertyOrder(value = "2")
    public final long id;

    @ApiModelProperty(value = "GeoJSON LineString Geometry Object. Points represent the link.", required = true, position = 3)
    @JsonPropertyOrder(value = "3")
    public final LineString geometry;

    @ApiModelProperty(value = "Link properties", required = true, position = 4)
    @JsonPropertyOrder(value = "4")
    public final LinkProperties properties;

    public LinkFeature(long id, LineString geometry, LinkProperties properties) {
        this.id = id;
        this.geometry = geometry;
        this.properties = properties;
    }
}
