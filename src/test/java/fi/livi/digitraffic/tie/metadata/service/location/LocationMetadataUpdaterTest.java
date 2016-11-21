package fi.livi.digitraffic.tie.metadata.service.location;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.base.AbstractTestBase;

public class LocationMetadataUpdaterTest extends AbstractTestBase {
    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;

    @Test
    @Rollback(true)
    @Transactional
    public void testfindAndUpdate() {
        locationMetadataUpdater.findAndUpdate();
    }
}
