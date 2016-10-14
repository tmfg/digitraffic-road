package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.File;
import java.nio.file.Path;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractMetadataTest;

public class LocationUpdaterTest extends AbstractMetadataTest {
    @Autowired
    private LocationUpdater locationUpdater;

    @Test
    public void testUpdateLocations() {
        final Path path = new File(getClass().getResource("/locations/Fi_Loc_singletable_ver2_4.xlsx").getFile()).toPath();

        locationUpdater.updateLocations(path);

    }

}
