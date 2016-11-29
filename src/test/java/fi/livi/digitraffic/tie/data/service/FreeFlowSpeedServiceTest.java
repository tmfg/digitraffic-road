package fi.livi.digitraffic.tie.data.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;

public class FreeFlowSpeedServiceTest extends MetadataIntegrationTest {

    @Autowired
    private FreeFlowSpeedService freeFlowSpeedService;

    @Test
    public void testListAllLinkDataFromNonObsoleteStations() {
        final FreeFlowSpeedRootDataObjectDto object = freeFlowSpeedService.listLinksPublicFreeFlowSpeeds(false);

        Assert.notNull(object);
        Assert.notNull(object.getDataUpdatedTime());

        Assert.notNull(object.getTmsFreeFlowSpeeds());
        Assert.notEmpty(object.getTmsFreeFlowSpeeds());

        Assert.notNull(object.getLinkFreeFlowSpeeds());
        Assert.notEmpty(object.getLinkFreeFlowSpeeds());
    }
}
