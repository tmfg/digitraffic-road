package fi.livi.digitraffic.tie.dto.weathercam.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureWithIdV1;
import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON CameraPresetFeature Object
 */
@Schema(description = "Weathercam GeoJSON Feature object base")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeathercamStationFeatureBaseV1<WeathercamStationPropertiesType extends RoadStationPropertiesSimpleV1<String>> extends FeatureWithIdV1<Point, WeathercamStationPropertiesType, String> {

    public WeathercamStationFeatureBaseV1(final Point geometry, final WeathercamStationPropertiesType properties) {
        super(geometry, properties);
    }

    @Schema(description = "Id of the road station", requiredMode = Schema.RequiredMode.REQUIRED)
    @Override
    public String getId() {
        return super.getId();
    }

    @Schema(description = "GeoJSON Point Geometry Object. Point where station is located", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "Point")
    @Override
    public Point getGeometry() {
        return super.getGeometry();
    }
}
