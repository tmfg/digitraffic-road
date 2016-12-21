package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.cxf.helpers.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.location.LocationVersionRepository;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;
import fi.livi.digitraffic.tie.metadata.model.location.LocationVersion;

@Service
public class LocationMetadataUpdater {
    private final LocationUpdater locationUpdater;
    private final LocationTypeUpdater locationTypeUpdater;
    private final LocationSubtypeUpdater locationSubtypeUpdater;

    private final LocationVersionRepository locationVersionRepository;

    private final MetadataFileFetcher metadataFileFetcher;

    private static final Logger log = LoggerFactory.getLogger(LocationMetadataUpdater.class);

    public LocationMetadataUpdater(final LocationUpdater locationUpdater,
                                   final LocationTypeUpdater locationTypeUpdater,
                                   final LocationSubtypeUpdater locationSubtypeUpdater,
                                   final LocationVersionRepository locationVersionRepository,
                                   final MetadataFileFetcher metadataFileFetcher) {
        this.locationUpdater = locationUpdater;
        this.locationTypeUpdater = locationTypeUpdater;
        this.locationSubtypeUpdater = locationSubtypeUpdater;
        this.locationVersionRepository = locationVersionRepository;
        this.metadataFileFetcher = metadataFileFetcher;
    }

    public void findAndUpdate() throws IOException {
        try {
            final MetadataVersions latestVersions = metadataFileFetcher.getLatestVersions();
            final LocationVersion currentVersion = locationVersionRepository.findLatestVersion();

            // check that new versions are all same
            if(!areVersionsSame(latestVersions)) {
                log.info("Different versions, locations {} and types {}", latestVersions.getLocationsVersion().version, latestVersions.getLocationTypeVersion().version);

                return;
            }

            if(isUpdateNeeded(latestVersions, currentVersion)) {
                final MetadataPathCollection paths = metadataFileFetcher.getFilePaths(latestVersions);
                final StopWatch stopWatch = StopWatch.createStarted();

                updateAll(paths.typesPath, paths.subtypesPath, paths.locationsPath, latestVersions);
                removeTempFiles(paths);
                stopWatch.stop();

                log.info("Locations and locationtypes updated, took {} millis", stopWatch.getTime());
            } else {
                log.info("No need to update locations or locationtypes");
            }
        } catch(final Exception e) {
            log.error("exception when fetching locations metadata", e);

            throw e;
        }
    }

    private boolean areVersionsSame(final MetadataVersions latestVersions) {
        return latestVersions != null && StringUtils.equals(latestVersions.getLocationsVersion().version, latestVersions.getLocationTypeVersion().version);
    }

    private void removeTempFiles(final MetadataPathCollection paths) {
        FileUtils.delete(paths.locationsPath.toFile());
        FileUtils.delete(paths.typesPath.toFile());
        FileUtils.delete(paths.subtypesPath.toFile());
    }

    private boolean isUpdateNeeded(final MetadataVersions latestVersions, final LocationVersion currentVersion) {
        return currentVersion == null ||
               needUpdate(latestVersions.getLocationsVersion(), currentVersion.getVersion()) ||
               needUpdate(latestVersions.getLocationTypeVersion(), currentVersion.getVersion());
    }

    private static boolean needUpdate(final MetadataVersions.MetadataVersion newVersion, final String currentVersion) {
        if(!StringUtils.equals(newVersion.version, currentVersion)) {
            log.info("Versions differ, old versions {}, new version {}, must be updated", currentVersion, newVersion.version);

            return true;
        }
        return false;
    }

    @Transactional
    public void updateAll(final Path locationTypePath, final Path locationSubtypePath, final Path locationPath,
                          final MetadataVersions latestVersions) {
        final String version = latestVersions.getLocationsVersion().version;

        locationTypeUpdater.updateLocationTypes(locationTypePath, version);
        final List<LocationSubtype> locationSubtypes = locationSubtypeUpdater.updateLocationSubtypes(locationSubtypePath, version);
        locationUpdater.updateLocations(locationPath, locationSubtypes, version);

        locationVersionRepository.save(new LocationVersion(version));
    }
}
