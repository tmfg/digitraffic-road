package fi.livi.digitraffic.tie.dto.weathercam.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GeoJSON CameraPresetFeature Object
 */
@Schema(description = " Weathercam station GeoJSON feature object with detailed information")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeathercamStationFeatureV1Detailed extends WeathercamStationFeatureBaseV1<WeathercamStationPropertiesDetailedV1> {

    public WeathercamStationFeatureV1Detailed(final Point geometry, final WeathercamStationPropertiesDetailedV1 properties) {
        super(geometry, properties);
    }
}
