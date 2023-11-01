package fi.livi.digitraffic.tie.dto.weather.forecast.v1;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON feature collection of forecast sections")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "features" })
public class ForecastSectionFeatureCollectionV1 extends FeatureCollectionV1<ForecastSectionFeatureV1> {

    public ForecastSectionFeatureCollectionV1(final Instant dataUpdatedTime, final List<ForecastSectionFeatureV1> features) {
        super(dataUpdatedTime, features);
    }

    public ForecastSectionFeatureCollectionV1(final Instant dataUpdatedTime) {
        this(dataUpdatedTime, Collections.emptyList());
    }
}
