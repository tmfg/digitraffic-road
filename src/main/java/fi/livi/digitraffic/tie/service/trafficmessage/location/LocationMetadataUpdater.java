package fi.livi.digitraffic.tie.service.trafficmessage.location;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.tie.dao.trafficmessage.location.LocationVersionRepository;
import fi.livi.digitraffic.tie.model.trafficmessage.location.Location;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationSubtype;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationType;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationVersion;

@ConditionalOnNotWebApplication
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

    @Transactional
    public void findAndUpdate() throws IOException {
        try {
            final MetadataVersions latestVersions = metadataFileFetcher.getLatestVersions();
            final LocationVersion currentVersion = locationVersionRepository.findLatestVersion();

            if (areVersionsEmpty(latestVersions)) {
                log.error("method=findAndUpdate empty metadataversions: {}", latestVersions);
            } else if (!areVersionsSame(latestVersions)) {
                log.info("method=findAndUpdate Different versions, locations {} and types {}",
                        latestVersions.getLocationsVersion().version, latestVersions.getLocationTypeVersion().version);
            } else if (isUpdateNeeded(latestVersions, currentVersion)) {
                final MetadataPathCollection paths = metadataFileFetcher.getFilePaths(latestVersions);
                final StopWatch stopWatch = StopWatch.createStarted();

                try {
                    updateAll(paths.typesPath, paths.subtypesPath, paths.locationsPath, latestVersions, paths);
                } finally {
                    // Always clean up temp files, even when updateAll() throws (parse failures etc.)
                    removeTempFiles(paths);
                }
                stopWatch.stop();

                log.info("method=findAndUpdate Locations and locationtypes updated, tookMs={}",
                        stopWatch.getDuration().toMillis());
            } else {
                log.info("method=findAndUpdate No need to update locations or locationtypes");
            }
        } catch (final Exception e) {
            log.error("method=findAndUpdate exception when fetching locations metadata", e);
            throw e;
        }
    }

    private boolean areVersionsEmpty(final MetadataVersions latestVersions) {
        return latestVersions == null || latestVersions.getLocationsVersion() == null ||
                latestVersions.getLocationTypeVersion() == null;
    }

    private boolean areVersionsSame(final MetadataVersions latestVersions) {
        return Strings.CS.equals(latestVersions.getLocationsVersion().version,
                latestVersions.getLocationTypeVersion().version);
    }

    private void removeTempFiles(final MetadataPathCollection paths) {
        FileUtils.deleteQuietly(paths.locationsPath.toFile());
        FileUtils.deleteQuietly(paths.typesPath.toFile());
        FileUtils.deleteQuietly(paths.subtypesPath.toFile());
    }

    private boolean isUpdateNeeded(final MetadataVersions latestVersions, final LocationVersion currentVersion) {
        return currentVersion == null ||
                needUpdate(latestVersions.getLocationsVersion(), currentVersion.getVersion()) ||
                needUpdate(latestVersions.getLocationTypeVersion(), currentVersion.getVersion());
    }

    private static boolean needUpdate(final MetadataVersions.MetadataVersion newVersion, final String currentVersion) {
        if (!Strings.CS.equals(newVersion.version, currentVersion)) {
            log.info("method=needUpdate Versions differ, oldVersion={}, newVersion={}, must be updated", currentVersion,
                    newVersion.version);
            return true;
        }
        return false;
    }

    private void updateAll(final Path locationTypePath, final Path locationSubtypePath, final Path locationPath,
                           final MetadataVersions latestVersions, final MetadataPathCollection paths) {
        final String version = latestVersions.getLocationsVersion().version;

        final ParseResult<LocationType> typesResult =
                locationTypeUpdater.updateLocationTypes(locationTypePath, paths.typesSource, version);
        final ParseResult<LocationSubtype> subtypesResult =
                locationSubtypeUpdater.updateLocationSubtypes(locationSubtypePath, paths.subtypesSource, version);
        final ParseResult<Location> locationsResult =
                locationUpdater.updateLocations(locationPath, paths.locationsSource, subtypesResult.items(),
                        paths.subtypesSource, version);

        final List<String> allErrors = collectErrors(typesResult, subtypesResult, locationsResult);

        if (!allErrors.isEmpty()) {
            log.error("method=updateAll version={} Parse failures detected – DB update rolled back. " +
                            "{} error(s) total:\n{}",
                    version, allErrors.size(), String.join("\n", allErrors));
            throw new IllegalStateException(
                    "Location metadata parse failures in version " + version +
                    ": " + allErrors.size() + " error(s) – see logs for details");
        }

        locationVersionRepository.save(new LocationVersion(version));
    }

    private static List<String> collectErrors(final ParseResult<LocationType> typesResult,
                                              final ParseResult<LocationSubtype> subtypesResult,
                                              final ParseResult<Location> locationsResult) {
        final List<String> all = new ArrayList<>();
        appendErrors(all, "TYPES",     typesResult.parseErrors());
        appendErrors(all, "SUBTYPES",  subtypesResult.parseErrors());
        appendErrors(all, "LOCATIONS", locationsResult.parseErrors());
        return all;
    }

    private static void appendErrors(final List<String> target, final String section,
                                     final List<String> errors) {
        if (!errors.isEmpty()) {
            target.add(StringUtil.format("{} ({} error(s)):", section, errors.size()));
            errors.forEach(e -> target.add("  " + e));
        }
    }
}
