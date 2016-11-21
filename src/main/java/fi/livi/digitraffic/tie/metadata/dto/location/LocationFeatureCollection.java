package fi.livi.digitraffic.tie.metadata.dto.location;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class LocationFeatureCollection {
    public final String type = "FeatureCollection";

    public final ZonedDateTime locationUpdateTime;

    public final List<LocationFeature> features;

    public LocationFeatureCollection(final ZonedDateTime locationUpdateTime,
                                     final List<LocationFeature> features) {
        this.locationUpdateTime = locationUpdateTime;
        this.features = features;
    }

    public LocationFeatureCollection(final ZonedDateTime locationUpdateTime) {
        this(locationUpdateTime, null);
    }
}
