package fi.livi.digitraffic.tie.dto.v1.location;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationSubtypeDtoV1;
import fi.livi.digitraffic.tie.dto.trafficmessage.v1.location.LocationTypeDtoV1;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Location types and location subtypes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationTypesMetadata {
    public final Instant typesUpdated;
    public final String typesVersion;

    public final List<LocationTypeDtoV1> locationTypes;
    public final List<LocationSubtypeDtoV1> locationSubtypes;

    public LocationTypesMetadata(final Instant typesUpdated,
                                 final String typesVersion,
                                 final List<LocationTypeDtoV1> locationTypes,
                                 final List<LocationSubtypeDtoV1> locationSubtypes) {
        this.typesUpdated = typesUpdated;
        this.typesVersion = typesVersion;
        this.locationTypes = locationTypes;
        this.locationSubtypes = locationSubtypes;
    }

    public LocationTypesMetadata(final Instant typesUpdated, final String typesVersion) {
        this(typesUpdated, typesVersion, null, null);
    }
}
