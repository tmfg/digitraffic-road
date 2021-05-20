package fi.livi.digitraffic.tie.data.service;

import org.junit.jupiter.api.Test;import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dto.v1.freeflowspeed.FreeFlowSpeedRootDataObjectDto;
import fi.livi.digitraffic.tie.service.v1.FreeFlowSpeedService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FreeFlowSpeedServiceTest extends AbstractServiceTest {

    @Autowired
    private FreeFlowSpeedService freeFlowSpeedService;

    @Test
    public void testListAllLinkDataFromNonObsoleteStations() {
        final FreeFlowSpeedRootDataObjectDto object = freeFlowSpeedService.listLinksPublicFreeFlowSpeeds(false);

        assertNotNull(object);
        assertNotNull(object.dataUpdatedTime);

        assertNotNull(object.getTmsFreeFlowSpeeds());
        assertTrue(object.getTmsFreeFlowSpeeds().size() > 0);
    }
}
