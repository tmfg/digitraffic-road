package fi.livi.digitraffic.tie.metadata.service.location;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;

public class LocationSubtypeUpdaterTest extends AbstractTest {
    @Autowired
    private LocationSubtypeUpdater locationSubtypeUpdater;

    public static final String SUBTYPES_FILE_NAME_OK = "/locations/SUBTYPES.DAT";
    public static final String SUBTYPES_FILE_NAME_ERROR = "/locations/SUBTYPES_ERROR.DAT";

    @Test
    @Transactional(readOnly = true)
    public void updateLocationSubtypesOk() {
        final Path path = new File(getClass().getResource(SUBTYPES_FILE_NAME_OK).getFile()).toPath();

        final List<LocationSubtype> subtypes = locationSubtypeUpdater.updateLocationSubtypes(path, "VERSION");
        assertThat(subtypes, hasSize(106));
    }

    @Test
    @Transactional(readOnly = true)
    public void updateLocationSubtypesError() {
        final Path path = new File(getClass().getResource(SUBTYPES_FILE_NAME_ERROR).getFile()).toPath();

        final List<LocationSubtype> subtypes = locationSubtypeUpdater.updateLocationSubtypes(path, "VERSION");
        assertThat(subtypes, hasSize(1));
    }
}
