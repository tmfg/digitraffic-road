package fi.livi.digitraffic.tie.metadata.geojson.forecastsection;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.v1.RootFeatureCollectionDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GeoJSON Feature Collection of road conditions", name = "ForecastSectionFeatureCollectionV1")
@JsonPropertyOrder({ "type", "dataUpdatedTime", "dataLastCheckedTime", "features" })
public class ForecastSectionFeatureCollection extends RootFeatureCollectionDto<ForecastSectionFeature> {

    public ForecastSectionFeatureCollection(final ZonedDateTime localTimestamp, final ZonedDateTime dataLastCheckedTime,
                                            final List<ForecastSectionFeature> features) {
        super(localTimestamp, dataLastCheckedTime, features);
    }
}
