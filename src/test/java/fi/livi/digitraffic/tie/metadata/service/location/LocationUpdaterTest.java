package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import fi.livi.digitraffic.tie.AbstractMetadataTest;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.metadata.dao.location.LocationTypeRepository;

public class LocationUpdaterTest extends AbstractMetadataTest {
    @Autowired
    private LocationUpdater locationUpdater;

    @Autowired
    private LocationTypeRepository locationTypeRepository;

    @Autowired
    private LocationSubtypeRepository locationSubtypeRepository;

    public static final String CSV_FILE_NAME = "/locations/FI_LC_noncertified_simple_1_11_30.csv";

    @Test
    @Transactional(readOnly = true)
    public void testUpdateLocations() throws IOException, SAXException {
        final Path path = new File(getClass().getResource(CSV_FILE_NAME).getFile()).toPath();

        locationUpdater.updateLocations(path, locationTypeRepository.findAll(), locationSubtypeRepository.findAll());
    }
}
