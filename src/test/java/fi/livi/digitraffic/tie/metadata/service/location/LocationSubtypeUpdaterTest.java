package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.File;
import java.nio.file.Path;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.AbstractMetadataTest;

public class LocationSubtypeUpdaterTest extends AbstractMetadataTest {
    @Autowired
    private LocationSubtypeUpdater locationSubtypeUpdater;

    @Test
    @Transactional(readOnly = true)
    public void testUpdateLocationSubtypes() {
        final Path path = new File(getClass().getResource("/locations/SUBTYPES.DAT").getFile()).toPath();

        locationSubtypeUpdater.updateLocationSubtypes(path);
    }

}
