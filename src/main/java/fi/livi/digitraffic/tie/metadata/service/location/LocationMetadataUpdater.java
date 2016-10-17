package fi.livi.digitraffic.tie.metadata.service.location;

import java.nio.file.Path;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class LocationMetadataUpdater {
    private final LocationUpdater locationUpdater;
    private final LocationTypeUpdater locationTypeUpdater;
    private final LocationSubtypeUpdater locationSubtypeUpdater;

    private final StaticDataStatusService staticDataStatusService;

    public LocationMetadataUpdater(final LocationUpdater locationUpdater,
                                   final LocationTypeUpdater locationTypeUpdater,
                                   final LocationSubtypeUpdater locationSubtypeUpdater,
                                   final StaticDataStatusService staticDataStatusService) {
        this.locationUpdater = locationUpdater;
        this.locationTypeUpdater = locationTypeUpdater;
        this.locationSubtypeUpdater = locationSubtypeUpdater;
        this.staticDataStatusService = staticDataStatusService;
    }

    @Transactional
    public void updateAll(final Path locationTypePath, final Path locationSubtypePath, final Path locationPath) {
        locationTypeUpdater.updateLocationTypes(locationTypePath);
        locationSubtypeUpdater.updateLocationSubtypes(locationSubtypePath);
        locationUpdater.updateLocations(locationPath);

        staticDataStatusService.updateMetadataUpdated(MetadataType.LOCATIONS);
    }
}
