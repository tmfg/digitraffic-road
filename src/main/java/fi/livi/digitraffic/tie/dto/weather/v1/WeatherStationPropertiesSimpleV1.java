package fi.livi.digitraffic.tie.dto.weather.v1;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.RoadStationPropertiesSimpleV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weather station properties object with basic information")
@JsonPropertyOrder({ "id", "name" })
public class WeatherStationPropertiesSimpleV1 extends RoadStationPropertiesSimpleV1<Long> {

    public WeatherStationPropertiesSimpleV1(final long id) {
        super(id);
    }
}
