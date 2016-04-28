package fi.livi.digitraffic.tie.data.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.data.model.FreeFlowSpeedDataObject;

public class FreeFlowSpeedServiceTest extends MetadataTest {
    @Autowired
    private FreeFlowSpeedService freeFlowSpeedService;

    @Test
    public void testListAllLamDataFromNonObsoleteStations() {
        final FreeFlowSpeedDataObject object = freeFlowSpeedService.listAllFreeFlowSpeeds();

        Assert.notNull(object);
        Assert.notNull(object.getDataLocalTime());
        Assert.notNull(object.getDataUtc());

        Assert.notNull(object.getLamData());
        Assert.notEmpty(object.getLamData());

        Assert.notNull(object.getLinkData());
        Assert.notEmpty(object.getLinkData());
    }
}
