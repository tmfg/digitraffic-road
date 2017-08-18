package fi.livi.digitraffic.tie.data.service.datex2;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2MessageType;

public class Datex2RoadworksIntegrationTest extends AbstractTest {
    @Autowired
    private Datex2RoadworksMessageUpdater messageUpdater;

    @Autowired
    private Datex2Repository datex2Repository;

    @Test
    @Rollback(false)
    public void updateMessages() {
        //assertEmpty(datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name()));

        messageUpdater.updateDatex2RoadworksMessages();

        Assert.assertTrue(datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name()).size() > 1);
    }
}
