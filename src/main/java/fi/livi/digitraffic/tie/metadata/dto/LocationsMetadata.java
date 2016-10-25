package fi.livi.digitraffic.tie.metadata.dto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import fi.livi.digitraffic.tie.data.dto.RootDataObjectDto;
import io.swagger.annotations.ApiModel;

@ApiModel(description = "Locations, location types and location subtypes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationsMetadata extends RootDataObjectDto {
    public final List<LocationTypeJson> locationTypes;
    public final List<LocationSubtypeJson> locationSubtypes;
    public final List<LocationJson> locations;

    public LocationsMetadata(final List<LocationTypeJson> locationTypes,
                             final List<LocationSubtypeJson> locationSubtypes,
                             final List<LocationJson> locations, final LocalDateTime lastUpdated) {
        super(lastUpdated);
        this.locationTypes = locationTypes;
        this.locationSubtypes = locationSubtypes;
        this.locations = locations;
    }

    public LocationsMetadata(final LocalDateTime updateTime) {
        this(null, null, null, updateTime);
    }

    public LocationsMetadata(final LocationJson location, final LocalDateTime updateTime) {
        this(null, null, Arrays.asList(location), updateTime);
    }
}
