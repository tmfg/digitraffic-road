package fi.livi.digitraffic.tie.data.service.datex2;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractTest;

public class Datex2RoadworksIntegrationTest extends AbstractTest {
    @Autowired
    private Datex2RoadworksMessageUpdater messageUpdater;

    @Test
    @Rollback(false)
    public void updateMessages() {
        messageUpdater.updateDatex2RoadworksMessages();
    }
}
