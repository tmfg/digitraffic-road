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
    private final String type = "Feature";

    @ApiModelProperty(value = "Link id", required = true, position = 2)
    @JsonPropertyOrder(value = "2")
    private long id;

    @ApiModelProperty(value = "GeoJSON LineString Geometry Object. Points represent the link.", required = true, position = 3)
    @JsonPropertyOrder(value = "3")
    private LineString geometry;

    @ApiModelProperty(value = "Link properties", required = true, position = 4)
    @JsonPropertyOrder(value = "4")
    private LinkProperties properties;

    public String getType() {
        return type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LineString getGeometry() {
        return geometry;
    }

    public void setGeometry(LineString geometry) {
        this.geometry = geometry;
    }

    public LinkProperties getProperties() {
        return properties;
    }

    public void setProperties(LinkProperties properties) {
        this.properties = properties;
    }
}
