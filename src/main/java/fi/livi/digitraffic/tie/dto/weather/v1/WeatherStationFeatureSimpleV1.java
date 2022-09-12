package fi.livi.digitraffic.tie.dto.weather.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weather station GeoJSON Feature object with basic information")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeatherStationFeatureSimpleV1 extends WeatherStationFeatureBaseV1<WeatherStationPropertiesSimpleV1> {

    public WeatherStationFeatureSimpleV1(final Point geometry, final WeatherStationPropertiesSimpleV1 properties) {
        super(geometry, properties);
    }
}
