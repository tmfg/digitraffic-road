package fi.livi.digitraffic.tie.dto.tms.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureWithIdV1;
import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON TMS station feature object
 */
@Schema(description = "Tms GeoJSON feature object base")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class TmsStationFeatureBaseV1<TmsStationPropertiesType extends RoadStationPropertiesSimpleV1<Long>> extends FeatureWithIdV1<Point, TmsStationPropertiesType, Long> {

    public TmsStationFeatureBaseV1(final Point geometry, final TmsStationPropertiesType properties) {
        super(geometry, properties);
    }

    @Schema(description = "Id of the road station", required = true)
    @Override
    public Long getId() {
        return super.getId();
    }

    @Schema(description = "GeoJSON Point Geometry Object. Point where station is located", required = true, allowableValues = "Point")
    @Override
    public Point getGeometry() {
        return super.getGeometry();
    }
}
