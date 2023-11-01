package fi.livi.digitraffic.tie.metadata.geojson.tms;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Feature;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON TmsStationFeature Object
 */
@Schema(description = "GeoJSON Feature Object", name = "TmsStationFeature")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class TmsStationFeature extends Feature<Point, TmsStationProperties> {

    // TODO: Remove this from next version as it is duplicated in properties
    @Schema(description = "Same as TmsStationProperties.roadStationId", required = true)
    @JsonPropertyOrder(value = "2")
    public final long id;

    public TmsStationFeature(final Point geometry, final TmsStationProperties properties, final long id) {
        super(geometry, properties);
        this.id = id;
    }

    @Schema(description = "GeoJSON Point Geometry Object. Point where station is located", required = true)
    @Override
    public Point getGeometry() {
        return super.getGeometry();
    }
}
