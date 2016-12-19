package fi.livi.digitraffic.tie.metadata.service.location;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.data.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationRepository;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationTypeRepository;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationFeature;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationFeatureCollection;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationJson;
import fi.livi.digitraffic.tie.metadata.dto.location.LocationTypesMetadata;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class LocationService {
    private final LocationTypeRepository locationTypeRepository;
    private final LocationSubtypeRepository locationSubtypeRepository;
    private final LocationRepository locationRepository;

    private final StaticDataStatusService staticDataStatusService;

    public LocationService(final LocationTypeRepository locationTypeRepository,
                           final LocationSubtypeRepository locationSubtypeRepository,
                           final LocationRepository locationRepository,
                           final StaticDataStatusService staticDataStatusService) {
        this.locationTypeRepository = locationTypeRepository;
        this.locationSubtypeRepository = locationSubtypeRepository;
        this.locationRepository = locationRepository;
        this.staticDataStatusService = staticDataStatusService;
    }

    @Transactional(readOnly = true)
    public LocationFeatureCollection findLocationsMetadata(final boolean onlyUpdateInfo) {
        final MetadataUpdated locationsUpdated = staticDataStatusService.getMetadataUpdatedTime(MetadataType.LOCATIONS);

        if(onlyUpdateInfo) {
            return new LocationFeatureCollection(locationsUpdated.getUpdatedTime(), locationsUpdated.getVersion());
        }

        return new LocationFeatureCollection(locationsUpdated.getUpdatedTime(), locationsUpdated.getVersion(),
                locationRepository.findAllProjectedBy().stream().map(LocationFeature::new).collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public LocationFeatureCollection findLocation(final int id) {
        final LocationJson location = locationRepository.findLocationByLocationCode(id);

        if(location == null) {
            throw new ObjectNotFoundException("Location", id);
        }

        final MetadataUpdated locationsUpdated = staticDataStatusService.getMetadataUpdatedTime(MetadataType.LOCATIONS);

        return new LocationFeatureCollection(locationsUpdated.getUpdatedTime(), locationsUpdated.getVersion(),
                Arrays.asList(new LocationFeature(location)));
    }

    public LocationTypesMetadata findLocationSubtypes(final boolean lastUpdated) {
        final MetadataUpdated typesUpdated = staticDataStatusService.getMetadataUpdatedTime(MetadataType.LOCATION_TYPES);
        final ZonedDateTime typesUpdateTime = typesUpdated.getUpdatedTime();

        if(lastUpdated) {
            return new LocationTypesMetadata(typesUpdateTime, typesUpdated.getVersion());
        }

        return new LocationTypesMetadata(typesUpdateTime, typesUpdated.getVersion(),
                locationTypeRepository.findAllProjectedBy(),
                locationSubtypeRepository.findAllProjectedBy());
    }
}
