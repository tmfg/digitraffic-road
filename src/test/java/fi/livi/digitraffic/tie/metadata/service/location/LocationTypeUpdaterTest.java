package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.File;
import java.nio.file.Path;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.AbstractMetadataTest;

public class LocationTypeUpdaterTest extends AbstractMetadataTest {
    @Autowired
    private LocationTypeUpdater locationTypeUpdater;

    @Test
    @Transactional(readOnly = true)
    public void testUpdateLocationTypes() {
        final Path path = new File(getClass().getResource("/locations/TYPES.DAT").getFile()).toPath();

        locationTypeUpdater.updateLocationTypes(path);
    }
}
