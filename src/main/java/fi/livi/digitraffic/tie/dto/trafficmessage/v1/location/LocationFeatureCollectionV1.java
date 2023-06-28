package fi.livi.digitraffic.tie.dto.trafficmessage.v1.location;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import fi.livi.digitraffic.tie.dto.geojson.v1.FeatureCollectionV1;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

@JsonPropertyOrder({"type", "locationsVersion"})
@Schema(description = "Location GeoJSON feature collection object")
public final class LocationFeatureCollectionV1 extends FeatureCollectionV1<LocationFeatureV1> {

    @Schema(description = "Locations version", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    public final String locationsVersion;

    public LocationFeatureCollectionV1(final Instant locationsUpdateTime,
                                       final String locationsVersion,
                                       final List<LocationFeatureV1> features) {
        super(locationsUpdateTime, features);
        this.locationsVersion = locationsVersion;
    }

    public LocationFeatureCollectionV1(final Instant locationsUpdateTime, final String locationsVersion) {
        this(locationsUpdateTime, locationsVersion, null);
    }
}
