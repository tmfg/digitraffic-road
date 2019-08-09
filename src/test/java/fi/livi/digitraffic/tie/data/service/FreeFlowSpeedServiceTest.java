package fi.livi.digitraffic.tie.data.service;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.data.dto.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;

@Import({FreeFlowSpeedService.class, DataStatusService.class})
public class FreeFlowSpeedServiceTest extends AbstractServiceTest {

    @Autowired
    private FreeFlowSpeedService freeFlowSpeedService;

    @Test
    public void testListAllLinkDataFromNonObsoleteStations() {
        final FreeFlowSpeedRootDataObjectDto object = freeFlowSpeedService.listLinksPublicFreeFlowSpeeds(false);

        Assert.assertNotNull(object);
        Assert.assertNotNull(object.getDataUpdatedTime());

        Assert.assertNotNull(object.getTmsFreeFlowSpeeds());
        Assert.assertTrue(object.getTmsFreeFlowSpeeds().size() > 0);
    }
}
