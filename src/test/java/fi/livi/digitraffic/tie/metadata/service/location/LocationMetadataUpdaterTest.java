package fi.livi.digitraffic.tie.metadata.service.location;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.base.AbstractTestBase;

//@Ignore
public class LocationMetadataUpdaterTest extends AbstractTestBase {
    @Autowired
    private LocationMetadataUpdater locationMetadataUpdater;

    @Test
    @Rollback(false)
    @Transactional
    public void testfindAndUpdate() {
        locationMetadataUpdater.findAndUpdate();
    }
}
