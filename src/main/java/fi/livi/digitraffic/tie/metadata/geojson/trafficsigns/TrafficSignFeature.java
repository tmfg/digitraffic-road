package fi.livi.digitraffic.tie.metadata.geojson.trafficsigns;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "GeoJSON Feature Object", value = "TrafficSignFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class TrafficSignFeature {
    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1, allowableValues = "Feature")
    @JsonPropertyOrder(value = "1")
    public final String type = "Feature";

    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3)
    @JsonPropertyOrder(value = "2")
    public final Point geometry;

    @ApiModelProperty(value = "Traffic Sign properties", required = true, position = 4)
    @JsonPropertyOrder(value = "3")
    public final TrafficSignProperties properties;

    public TrafficSignFeature(final Point geometry, final TrafficSignProperties properties) {
        this.geometry = geometry;
        this.properties = properties;
    }
}
