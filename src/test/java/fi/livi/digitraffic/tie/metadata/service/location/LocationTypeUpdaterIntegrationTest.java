package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.File;
import java.nio.file.Path;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.base.AbstractMetadataIntegrationTest;

public class LocationTypeUpdaterIntegrationTest extends AbstractMetadataIntegrationTest {
    @Autowired
    private LocationTypeUpdater locationTypeUpdater;

    public static final String TYPES_FILE_NAME = "/locations/TYPES.DAT";

    @Test
    @Transactional(readOnly = true)
    public void testUpdateLocationTypes() {
        final Path path = new File(getClass().getResource(TYPES_FILE_NAME).getFile()).toPath();

        locationTypeUpdater.updateLocationTypes(path, "testVersion");
    }
}
