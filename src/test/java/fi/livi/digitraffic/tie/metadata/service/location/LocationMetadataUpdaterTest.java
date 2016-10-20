package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import fi.livi.digitraffic.tie.AbstractMetadataTest;

@Ignore
public class LocationMetadataUpdaterTest extends AbstractMetadataTest {
    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;

    @Test
    @Rollback(true)
    @Transactional
    public void testUpdateAll() throws IOException, OpenXML4JException, SAXException {
        final Path locationsPath = new File(getClass().getResource(LocationUpdaterTest.XLSX_FILE_NAME).getFile()).toPath();
        final Path typesPath = new File(getClass().getResource(LocationTypeUpdaterTest.TYPES_FILE_NAME).getFile()).toPath();
        final Path subtypesPath = new File(getClass().getResource(LocationSubtypeUpdaterTest.SUBTYPES_FILE_NAME).getFile()).toPath();

        locationMetadataUpdater.updateAll(typesPath, subtypesPath, locationsPath);
    }

    @Test
    @Rollback(false)
    @Transactional
    public void testfindAndUpdate() {
        locationMetadataUpdater.findAndUpdate();
    }
}
