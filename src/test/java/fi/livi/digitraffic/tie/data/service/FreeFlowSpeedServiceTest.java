package fi.livi.digitraffic.tie.data.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;

public class FreeFlowSpeedServiceTest extends AbstractTest {

    @Autowired
    private FreeFlowSpeedService freeFlowSpeedService;

    @Test
    public void testListAllLinkDataFromNonObsoleteStations() {
        final FreeFlowSpeedRootDataObjectDto object = freeFlowSpeedService.listLinksPublicFreeFlowSpeeds(false);

        Assert.assertNotNull(object);
        Assert.assertNotNull(object.getDataUpdatedTime());

        Assert.assertNotNull(object.getTmsFreeFlowSpeeds());
        Assert.assertTrue(object.getTmsFreeFlowSpeeds().size() > 0);

        Assert.assertNotNull(object.getLinkFreeFlowSpeeds());
        Assert.assertTrue(object.getLinkFreeFlowSpeeds().size() > 0);
    }
}
