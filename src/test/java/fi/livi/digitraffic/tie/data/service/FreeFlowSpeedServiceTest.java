package fi.livi.digitraffic.tie.data.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.data.model.FreeFlowSpeedObject;

public class FreeFlowSpeedServiceTest extends MetadataTest {
    @Autowired
    private FreeFlowSpeedService freeFlowSpeedService;

    @Test
    public void testListAllLamDataFromNonObsoleteStations() {
        final FreeFlowSpeedObject object = freeFlowSpeedService.listAllFreeFlowSpeeds();

        Assert.notNull(object);
        Assert.notNull(object.getLocalTime());
        Assert.notNull(object.getUtc());

        Assert.notNull(object.getLamData());
        Assert.notEmpty(object.getLamData());

        Assert.notNull(object.getLinkData());
        Assert.notEmpty(object.getLinkData());
    }
}
