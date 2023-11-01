package fi.livi.digitraffic.tie.service.trafficmessage.location;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.trafficmessage.location.LocationType;

public class LocationTypeUpdaterTest extends AbstractServiceTest {
    @Autowired
    private LocationTypeUpdater locationTypeUpdater;

    public static final String TYPES_FILE_NAME = "/locations/TYPES.DAT";

    @Test
    public void testUpdateLocationTypes() {
        final Path path = new File(Objects.requireNonNull(getClass().getResource(TYPES_FILE_NAME)).getFile()).toPath();

        final List<LocationType> updated = locationTypeUpdater.updateLocationTypes(path, "testVersion");
        Assertions.assertNotEquals(0, updated.size());
    }
}
