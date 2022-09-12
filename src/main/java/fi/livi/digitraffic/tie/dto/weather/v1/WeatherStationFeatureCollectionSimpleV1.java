package fi.livi.digitraffic.tie.dto.weather.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of weather stations")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "features" })
public class WeatherStationFeatureCollectionSimpleV1 extends FeatureCollectionV1<WeatherStationFeatureSimpleV1> {

    public WeatherStationFeatureCollectionSimpleV1(final Instant dataUpdatedTime, final Instant dataLastCheckedTime,
                                                   final List<WeatherStationFeatureSimpleV1> features) {
        super(dataUpdatedTime, dataLastCheckedTime, features);
    }
}
