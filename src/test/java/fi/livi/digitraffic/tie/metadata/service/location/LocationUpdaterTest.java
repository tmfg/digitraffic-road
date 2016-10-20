package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import fi.livi.digitraffic.tie.AbstractMetadataTest;

public class LocationUpdaterTest extends AbstractMetadataTest {
    @Autowired
    private LocationUpdater locationUpdater;

    public static final String XLSX_FILE_NAME = "/locations/Fi_Loc_singletable_ver2_4.xlsx";

    @Test
    @Transactional(readOnly = true)
    public void testUpdateLocations() throws IOException, OpenXML4JException, SAXException {
        final Path path = new File(getClass().getResource(XLSX_FILE_NAME).getFile()).toPath();

        locationUpdater.updateLocations(path);
    }
}
