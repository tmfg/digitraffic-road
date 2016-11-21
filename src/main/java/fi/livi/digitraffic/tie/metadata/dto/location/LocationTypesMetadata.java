package fi.livi.digitraffic.tie.metadata.dto.location;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Location types and location subtypes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationTypesMetadata {
    public final ZonedDateTime typesUpdated;

    public final List<LocationTypeJson> locationTypes;
    public final List<LocationSubtypeJson> locationSubtypes;

    public LocationTypesMetadata(final ZonedDateTime typesUpdated,
                                 final List<LocationTypeJson> locationTypes,
                                 final List<LocationSubtypeJson> locationSubtypes) {
        this.typesUpdated = typesUpdated;

        this.locationTypes = locationTypes;
        this.locationSubtypes = locationSubtypes;
    }

    public LocationTypesMetadata(final ZonedDateTime typesUpdated) {
        this(typesUpdated, null, null);
    }
}
