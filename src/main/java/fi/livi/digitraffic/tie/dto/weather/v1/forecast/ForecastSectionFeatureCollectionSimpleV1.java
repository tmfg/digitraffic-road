package fi.livi.digitraffic.tie.dto.weather.v1.forecast;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON feature collection of simple forecast sections")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "features" })
public class ForecastSectionFeatureCollectionSimpleV1 extends FeatureCollectionV1<ForecastSectionFeatureSimpleV1> {

    public ForecastSectionFeatureCollectionSimpleV1(final Instant dataUpdatedTime, final List<ForecastSectionFeatureSimpleV1> features) {
        super(dataUpdatedTime, features);
    }

    public ForecastSectionFeatureCollectionSimpleV1(final Instant dataUpdatedTime) {
        this(dataUpdatedTime, Collections.emptyList());
    }
}
