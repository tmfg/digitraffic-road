package fi.livi.digitraffic.tie.dto.weather.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureWithIdV1;
import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesSimpleV1;
import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON weather station object
 */
@Schema(description = "Weather station GeoJSON feature object base")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeatherStationFeatureBaseV1<WeatherStationPropertiesType extends RoadStationPropertiesSimpleV1<Long>> extends FeatureWithIdV1<Point, WeatherStationPropertiesType, Long> {

    public WeatherStationFeatureBaseV1(final Point geometry, final WeatherStationPropertiesType properties) {
        super(geometry, properties);
    }

    @Schema(description = "Id of the road station", requiredMode = Schema.RequiredMode.REQUIRED)
    @Override
    public Long getId() {
        return super.getId();
    }

    @Schema(description = "GeoJSON Point Geometry Object. Point where station is located", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "Point")
    @Override
    public Point getGeometry() {
        return super.getGeometry();
    }
}
