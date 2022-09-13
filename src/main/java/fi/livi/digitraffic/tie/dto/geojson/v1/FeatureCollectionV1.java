package fi.livi.digitraffic.tie.dto.geojson.v1;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.metadata.geojson.FeatureCollection;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({ "dataUpdatedTime", "type", "features" })
@Schema(description = "GeoJSON Feature Collection Object")
public class FeatureCollectionV1<FeatureType> extends FeatureCollection<FeatureType> {

    @Schema(description = "Data last updated date time", required = true)
    public final Instant dataUpdatedTime;

    /**
     * Removed to enable ETag -requests effectively
     * @see <a href="https://www.digitraffic.fi/en/instructions/#avoiding-unnecessary-data-transfer-in-weather-camera-requests">digitraffic.fi - ETag usage</a>
     * @see DPO-1946
     */
    @JsonIgnore
    @Schema(description = "Data last checked date time", required = true)
    public final Instant dataLastCheckedTime;

    public FeatureCollectionV1(final Instant dataUpdatedTime,
                               final Instant dataLastCheckedTime,
                               final List<FeatureType> features) {
        super(features);
        this.dataUpdatedTime = dataUpdatedTime;
        this.dataLastCheckedTime = dataLastCheckedTime;
    }
}
