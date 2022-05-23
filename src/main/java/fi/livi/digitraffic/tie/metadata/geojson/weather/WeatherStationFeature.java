package fi.livi.digitraffic.tie.metadata.geojson.weather;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON WeatherStation Feature Object
 */
@Schema(description = "GeoJSON Feature Object of Weather Station", name = "WeatherStationFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeatherStationFeature extends Feature<Point, WeatherStationProperties> {

    // TODO: Remove this from next version as it is duplicated in properties
    @Schema(description = "Road station id, same as WeatherStationProperties.roadStationId", required = true)
    private long id;

    public WeatherStationFeature(final Point geometry, final WeatherStationProperties properties, final long id) {
        super(geometry, properties);
        this.id = id;
    }

    @Schema(description = "GeoJSON Point Geometry Object. Point where station is located", required = true)
    @Override
    public Point getGeometry() {
        return super.getGeometry();
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }
}
