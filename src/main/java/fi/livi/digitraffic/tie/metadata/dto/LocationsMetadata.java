package fi.livi.digitraffic.tie.metadata.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;
import fi.livi.digitraffic.tie.metadata.model.location.LocationType;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Locations, location types and location subtypes")
@JsonInclude(JsonInclude.Include.NON_NULL)
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
        this(null, null, null, updateTime);
    }

    public LocationsMetadata(final LocationJsonObject location, final LocalDateTime updateTime) {
        this(null, null, Arrays.asList(location), updateTime);
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
