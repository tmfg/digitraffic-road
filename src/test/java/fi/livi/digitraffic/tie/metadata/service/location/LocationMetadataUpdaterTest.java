package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import fi.livi.digitraffic.tie.base.AbstractTestBase;

@Ignore
public class LocationMetadataUpdaterTest extends AbstractTestBase {
    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;

    @Test
    @Rollback(true)
    @Transactional
    public void testUpdateAll() throws IOException, SAXException {
        final Path locationsPath = new File(getClass().getResource(LocationUpdaterTest.CSV_FILE_NAME).getFile()).toPath();
        final Path typesPath = new File(getClass().getResource(LocationTypeUpdaterTest.TYPES_FILE_NAME).getFile()).toPath();
        final Path subtypesPath = new File(getClass().getResource(LocationSubtypeUpdaterTest.SUBTYPES_FILE_NAME).getFile()).toPath();

        locationMetadataUpdater.updateAll(typesPath, subtypesPath, locationsPath, "T1", "T2");
    }

    @Test
    @Rollback(false)
    @Transactional
    public void testfindAndUpdate() {
        locationMetadataUpdater.findAndUpdate();
    }
}
