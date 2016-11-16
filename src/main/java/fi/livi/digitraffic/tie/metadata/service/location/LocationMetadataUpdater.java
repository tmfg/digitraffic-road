package fi.livi.digitraffic.tie.metadata.service.location;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;
import fi.livi.digitraffic.tie.metadata.service.StaticDataStatusService;

@Service
public class LocationMetadataUpdater {
    private final LocationUpdater locationUpdater;
    private final LocationTypeUpdater locationTypeUpdater;
    private final LocationSubtypeUpdater locationSubtypeUpdater;

    private final StaticDataStatusService staticDataStatusService;

    private final MetadataFileFetcher metadataFileFetcher;

    private static final Logger log = LoggerFactory.getLogger(LocationMetadataUpdater.class);

    public LocationMetadataUpdater(final LocationUpdater locationUpdater,
                                   final LocationTypeUpdater locationTypeUpdater,
                                   final LocationSubtypeUpdater locationSubtypeUpdater,
                                   final StaticDataStatusService staticDataStatusService,
                                   final MetadataFileFetcher metadataFileFetcher) {
        this.locationUpdater = locationUpdater;
        this.locationTypeUpdater = locationTypeUpdater;
        this.locationSubtypeUpdater = locationSubtypeUpdater;
        this.staticDataStatusService = staticDataStatusService;
        this.metadataFileFetcher = metadataFileFetcher;
    }

    public void findAndUpdate() {
        try {
            final MetadataVersions latestVersions = metadataFileFetcher.getLatestVersions();
            final MetadataVersions currentVersions = staticDataStatusService.getCurrentMetadataVersions();

            if(isUpdateNeeded(latestVersions, currentVersions)) {
                final MetadataPathCollection paths = metadataFileFetcher.getFilePaths(latestVersions);
                final StopWatch stopWatch = new StopWatch();

                stopWatch.start();
                updateAll(paths.typesPath, paths.subtypesPath, paths.locationsPath, latestVersions);
                stopWatch.stop();

                log.info(String.format("Locations and locationtypes updated, took %d millis", stopWatch.getTime()));
            } else {
                log.info("No need to update locations or locationtypes");
            }
        } catch(final Exception e) {
            log.error("exception when fetching locations metadata", e);
        }
    }

    private boolean isUpdateNeeded(final MetadataVersions latestVersions, final MetadataVersions currentVersions) {
        return needUpdate(latestVersions.getLocationsVersion(), currentVersions.getLocationsVersion()) ||
               needUpdate(latestVersions.getLocationTypeVersion(), currentVersions.getLocationTypeVersion());
    }

    private boolean needUpdate(final MetadataVersions.MetadataVersion oldVersion, final MetadataVersions.MetadataVersion newVersion) {
        if(!StringUtils.equals(oldVersion.version, newVersion.version)) {
            log.info(String.format("Versions differ, old versions %s, new version %s, must be updated", oldVersion, newVersion));

            return true;
        }
        return false;
    }

    @Transactional
    public void updateAll(final Path locationTypePath, final Path locationSubtypePath, final Path locationPath,
                          final MetadataVersions latestVersions) {
        locationTypeUpdater.updateLocationTypes(locationTypePath);
        final List<LocationSubtype> locationSubtypes = locationSubtypeUpdater.updateLocationSubtypes(locationSubtypePath);
        locationUpdater.updateLocations(locationPath, locationSubtypes);

        staticDataStatusService.updateMetadataUpdated(MetadataType.LOCATION_TYPES, latestVersions.getLocationTypeVersion().version);
        staticDataStatusService.updateMetadataUpdated(MetadataType.LOCATIONS, latestVersions.getLocationsVersion().version);
    }
}
