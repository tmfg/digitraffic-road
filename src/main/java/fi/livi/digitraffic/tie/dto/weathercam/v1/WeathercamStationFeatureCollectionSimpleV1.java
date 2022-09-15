package fi.livi.digitraffic.tie.dto.weathercam.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Weathercam GeoJSON FeatureCollection object with basic information")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class WeathercamStationFeatureCollectionSimpleV1 extends FeatureCollectionV1<WeathercamStationFeatureSimpleV1> {

    public WeathercamStationFeatureCollectionSimpleV1(final Instant updatedTime,
                                                      final List<WeathercamStationFeatureSimpleV1> features) {
        super(updatedTime, features);
    }
}
