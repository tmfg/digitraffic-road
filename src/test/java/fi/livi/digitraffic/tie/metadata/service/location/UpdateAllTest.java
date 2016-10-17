package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.File;
import java.nio.file.Path;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.AbstractMetadataTest;

public class UpdateAllTest extends AbstractMetadataTest {
    @Autowired
    private LocationUpdater locationUpdater;

    @Autowired
    private LocationTypeUpdater locationTypeUpdater;

    @Autowired
    private LocationSubtypeUpdater locationSubtypeUpdater;

    @Test
    @Rollback(false)
    @Transactional
    public void testUpdateAll() {
        final Path locationsPath = new File(getClass().getResource(LocationUpdaterTest.XLSX_FILE_NAME).getFile()).toPath();
        final Path typesPath = new File(getClass().getResource(LocationTypeUpdaterTest.TYPES_FILE_NAME).getFile()).toPath();
        final Path subtypesPath = new File(getClass().getResource(LocationSubtypeUpdaterTest.SUBTYPES_FILE_NAME).getFile()).toPath();

        locationTypeUpdater.updateLocationTypes(typesPath);
        locationSubtypeUpdater.updateLocationSubtypes(subtypesPath);
        locationUpdater.updateLocations(locationsPath);

//        throw new IllegalArgumentException("rollback please");
    }

}
