package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.File;
import java.nio.file.Path;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.base.AbstractMetadataIntegrationTest;

public class LocationSubtypeUpdaterIntegrationTest extends AbstractMetadataIntegrationTest {
    @Autowired
    private LocationSubtypeUpdater locationSubtypeUpdater;

    public static final String SUBTYPES_FILE_NAME = "/locations/SUBTYPES.DAT";

    @Test
    @Transactional(readOnly = true)
    public void testUpdateLocationSubtypes() {
        final Path path = new File(getClass().getResource(SUBTYPES_FILE_NAME).getFile()).toPath();

        locationSubtypeUpdater.updateLocationSubtypes(path, "VERSION");
    }
}
