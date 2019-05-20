package fi.livi.digitraffic.tie.metadata.geojson.weather;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GeoJSON WeatherStation Feature Object
 */
@ApiModel(description = "GeoJSON Feature Object of Weather Station", value = "WeatherStationFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeatherStationFeature implements Feature<Point> {

    @ApiModelProperty(value = "\"Feature\": GeoJSON Feature Object", required = true, position = 1, allowableValues = "Feature")
    private final String type = "Feature";

    @ApiModelProperty(value = "Road station id, same as WeatherStationProperties.roadStationId", required = true, position = 2)
    private long id;

    @ApiModelProperty(value = "GeoJSON Point Geometry Object. Point where station is located", required = true, position = 3)
    private Point geometry;

    @ApiModelProperty(value = "Weather station properties", required = true, position = 4)
    private WeatherStationProperties properties = new WeatherStationProperties();

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Point getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(final Point geometry) {
        this.geometry = geometry;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public WeatherStationProperties getProperties() {
        return properties;
    }

    public void setProperties(final WeatherStationProperties properties) {
        this.properties = properties;
    }

}
