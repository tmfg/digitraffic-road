package fi.livi.digitraffic.tie.dto.geojson.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.common.dto.LastModifiedSupport;
import fi.livi.digitraffic.tie.metadata.geojson.FeatureCollection;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "dataUpdatedTime", "type", "features" })
@Schema(description = "GeoJSON Feature Collection Object")
public class FeatureCollectionV1<FeatureType> extends FeatureCollection<FeatureType> implements LastModifiedSupport {

    @Schema(description = "Data last updated date time", required = true)
    public final Instant dataUpdatedTime;

    public FeatureCollectionV1(final Instant dataUpdatedTime,
                               final List<FeatureType> features) {
        super(features);
        this.dataUpdatedTime = dataUpdatedTime;
    }

    @Override
    public Instant getLastModified() {
        return dataUpdatedTime;
    }
}
