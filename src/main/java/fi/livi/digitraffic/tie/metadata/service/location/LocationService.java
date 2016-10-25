package fi.livi.digitraffic.tie.metadata.service.location;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.location.LocationRepository;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationTypeRepository;
import fi.livi.digitraffic.tie.metadata.dto.LocationJson;
import fi.livi.digitraffic.tie.metadata.dto.LocationsMetadata;
import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.MetadataUpdated;
import fi.livi.digitraffic.tie.metadata.model.location.Location;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;
import fi.livi.digitraffic.tie.metadata.model.location.LocationType;
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
    public List<LocationType> listAllLocationTypes() {
        return locationTypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<LocationSubtype> listAllLocationSubTypes() {
        return locationSubtypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Location> listAllLocations() {
        return locationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public LocationsMetadata findLocationsMetadata(final boolean onlyUpdateInfo) {
        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.LOCATIONS);
        final LocalDateTime updateTime = updated == null ? null : updated.getUpdated();

        if(onlyUpdateInfo) {
            return new LocationsMetadata(updateTime);
        }

        return new LocationsMetadata(
                locationTypeRepository.streamAllProjectedBy().collect(Collectors.toList()),
                locationSubtypeRepository.streamAllProjectedBy().collect(Collectors.toList()),
                locationRepository.streamAllProjectedBy().collect(Collectors.toList()),
                updateTime);
    }

    @Transactional(readOnly = true)
    public LocationsMetadata findLocation(final int id) {
        final MetadataUpdated updated = staticDataStatusService.findMetadataUpdatedByMetadataType(MetadataType.LOCATIONS);
        final LocalDateTime updateTime = updated == null ? null : updated.getUpdated();

        final LocationJson location = locationRepository.findLocationByLocationCode(id);

        return location != null ? new LocationsMetadata(location, updateTime) : new LocationsMetadata(updateTime);
    }
}
