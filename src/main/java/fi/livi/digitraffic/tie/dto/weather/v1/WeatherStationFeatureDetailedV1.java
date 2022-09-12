package fi.livi.digitraffic.tie.dto.weather.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Detailed Weather station feature object
 */
@Schema(description = "Weather station GeoJSON feature object with detailed information")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeatherStationFeatureDetailedV1 extends WeatherStationFeatureBaseV1<WeatherStationPropertiesDetailedV1> {

    public WeatherStationFeatureDetailedV1(final Point geometry, final WeatherStationPropertiesDetailedV1 properties) {
        super(geometry, properties);
    }
}