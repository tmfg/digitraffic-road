package fi.livi.digitraffic.tie.service.v1.location;

import java.io.File;
import java.nio.file.Path;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;

public class LocationTypeUpdaterTest extends AbstractServiceTest {
    @Autowired
    private LocationTypeUpdater locationTypeUpdater;

    public static final String TYPES_FILE_NAME = "/locations/TYPES.DAT";

    @Test
    public void testUpdateLocationTypes() {
        final Path path = new File(getClass().getResource(TYPES_FILE_NAME).getFile()).toPath();

        locationTypeUpdater.updateLocationTypes(path, "testVersion");
    }
}
