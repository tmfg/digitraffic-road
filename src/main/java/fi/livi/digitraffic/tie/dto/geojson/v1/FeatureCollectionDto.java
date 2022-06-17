package fi.livi.digitraffic.tie.dto.geojson.v1;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.FeatureCollection;
import io.swagger.v3.oas.annotations.media.Schema;

@Immutable
@JsonPropertyOrder({ "dataUpdatedTime", "dataLastCheckedTime", "type", "features" })
@Schema(description = "GeoJSON Feature Collection Object", name = "FeatureCollectionV1")
public class FeatureCollectionDto<FeatureType> extends FeatureCollection<FeatureType> {

    @Schema(description = "Data last updated date time", required = true)
    public final Instant dataUpdatedTime;

    @Schema(description = "Data last checked date time", required = true)
    public final Instant dataLastCheckedTime;

    public FeatureCollectionDto(final Instant dataUpdatedTime,
                                final Instant dataLastCheckedTime,
                                final List<FeatureType> features) {
        super(features);
        this.dataUpdatedTime = dataUpdatedTime;
        this.dataLastCheckedTime = dataLastCheckedTime;
    }
}
