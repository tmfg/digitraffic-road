package fi.livi.digitraffic.tie.metadata.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;
import fi.livi.digitraffic.tie.metadata.model.location.LocationType;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Locations, location types and location subtypes")
public class LocationsMetadata extends RootDataObjectDto {
    private final List<LocationType> locationTypes;
    private final List<LocationSubtype> locationSubtypes;
    private final List<LocationJsonObject> locations;

    public LocationsMetadata(final List<LocationType> locationTypes, final List<LocationSubtype> locationSubtypes,
                             final List<LocationJsonObject> locations, final LocalDateTime lastUpdated) {
        super(lastUpdated);
        this.locationTypes = locationTypes;
        this.locationSubtypes = locationSubtypes;
        this.locations = locations;
    }

    public LocationsMetadata(final LocalDateTime updateTime) {
        this(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), updateTime);
    }

    public List<LocationSubtype> getLocationSubtypes() {
        return locationSubtypes;
    }

    public List<LocationType> getLocationTypes() {
        return locationTypes;
    }

    public List<LocationJsonObject> getLocations() {
        return locations;
    }
}
