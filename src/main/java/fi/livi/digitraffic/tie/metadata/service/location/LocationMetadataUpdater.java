package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import fi.livi.digitraffic.tie.metadata.model.MetadataType;
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
                                   final StaticDataStatusService staticDataStatusService, MetadataFileFetcher metadataFileFetcher) {
        this.locationUpdater = locationUpdater;
        this.locationTypeUpdater = locationTypeUpdater;
        this.locationSubtypeUpdater = locationSubtypeUpdater;
        this.staticDataStatusService = staticDataStatusService;
        this.metadataFileFetcher = metadataFileFetcher;
    }

    public void findAndUpdate() {
        try {
            final MetadataPathCollection paths = metadataFileFetcher.getFilePaths();

            updateAll(paths.typesPath, paths.subtypesPath, paths.locationsPath);
        } catch(final Exception e) {
            log.error("exception when fetching locations metadata", e);
        }
    }

    @Transactional
    public void updateAll(final Path locationTypePath, final Path locationSubtypePath, final Path locationPath)
            throws IOException, OpenXML4JException, SAXException {
        locationTypeUpdater.updateLocationTypes(locationTypePath);
        locationSubtypeUpdater.updateLocationSubtypes(locationSubtypePath);
        locationUpdater.updateLocations(locationPath);

        staticDataStatusService.updateMetadataUpdated(MetadataType.LOCATIONS);
    }
}
