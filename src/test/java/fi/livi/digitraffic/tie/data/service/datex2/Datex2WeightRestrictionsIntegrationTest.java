package fi.livi.digitraffic.tie.data.service.datex2;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.Rollback;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dao.Datex2Repository;
import fi.livi.digitraffic.tie.data.model.Datex2MessageType;

public class Datex2WeightRestrictionsIntegrationTest extends AbstractTest {
    @Autowired
    private Datex2SimpleMessageUpdater messageUpdater;

    @Autowired
    private Datex2Repository datex2Repository;

    @SpyBean
    private Datex2WeightRestrictionsHttpClient datex2WeightRestrictionsHttpClient;

    @Test
    @Rollback(false)
    public void updateMessagesWithRealData() {
        //assertEmpty(datex2Repository.findAllActive(Datex2MessageType.ROADWORK.name()));

        messageUpdater.updateDatex2WeightRestrictionMessages();

        Assert.assertTrue(datex2Repository.findAllActive(Datex2MessageType.WEIGHT_RESTRICTION.name()).size() > 0);
    }

}
