package fi.livi.digitraffic.tie.dto.weathercam.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.Point;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam station GeoJSON Feature object with basic information")
@JsonPropertyOrder({ "type", "id", "geometry", "properties" })
public class WeathercamStationFeatureSimpleV1 extends WeathercamStationFeatureBaseV1<WeathercamStationPropertiesSimpleV1> {


    public WeathercamStationFeatureSimpleV1(final Point geometry, final WeathercamStationPropertiesSimpleV1 properties) {
        super(geometry, properties);
    }
}
