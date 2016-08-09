package fi.livi.digitraffic.tie.data.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;

public class FreeFlowSpeedServiceTest extends MetadataTest {

    @Autowired
    private FreeFlowSpeedService freeFlowSpeedService;

    @Test
    public void testListAllLamDataFromNonObsoleteStations() {
        final FreeFlowSpeedRootDataObjectDto object = freeFlowSpeedService.listLinksPublicFreeFlowSpeeds(false);

        Assert.notNull(object);
        Assert.notNull(object.getDataUptadedLocalTime());
        Assert.notNull(object.getDataUptadedUtc());

        Assert.notNull(object.getLamFreeFlowSpeeds());
        Assert.notEmpty(object.getLamFreeFlowSpeeds());

        Assert.notNull(object.getLinkFreeFlowSpeeds());
        Assert.notEmpty(object.getLinkFreeFlowSpeeds());
    }
}
