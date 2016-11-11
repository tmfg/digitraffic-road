package fi.livi.digitraffic.tie.metadata.dto;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Locations, location types and location subtypes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationsMetadata {
    public final ZonedDateTime locationsUpdated;
    public final ZonedDateTime typesUpdated;

    public final List<LocationTypeJson> locationTypes;
    public final List<LocationSubtypeJson> locationSubtypes;
    public final List<LocationJson> locations;

    public LocationsMetadata(final ZonedDateTime locationsUpdated, final ZonedDateTime typesUpdated,
                             final List<LocationTypeJson> locationTypes,
                             final List<LocationSubtypeJson> locationSubtypes,
                             final List<LocationJson> locations) {
        this.locationsUpdated = locationsUpdated;
        this.typesUpdated = typesUpdated;

        this.locationTypes = locationTypes;
        this.locationSubtypes = locationSubtypes;
        this.locations = locations;
    }

    public LocationsMetadata(final ZonedDateTime locationsUpdated, final ZonedDateTime typesUpdated) {
        this(locationsUpdated, typesUpdated, null, null, null);
    }

    public LocationsMetadata(final ZonedDateTime locationsUpdated, final ZonedDateTime typesUpdated, final LocationJson location) {
        this(locationsUpdated, typesUpdated, null, null, Collections.singletonList(location));
    }
}
