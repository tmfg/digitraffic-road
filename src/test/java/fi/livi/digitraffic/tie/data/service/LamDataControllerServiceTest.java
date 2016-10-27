package fi.livi.digitraffic.tie.data.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;

public class LamDataControllerServiceTest extends MetadataIntegrationTest {

    @Autowired
    private LamDataService lamDataService;

    @Test
    public void testListAllLamDataFromNonObsoleteStations()  {
        final LamRootDataObjectDto object = lamDataService.findPublicLamData(false);

        Assert.notNull(object);
        Assert.notNull(object.getDataUpdatedLocalTime());
        Assert.notNull(object.getDataUpdatedUtc());
        Assert.notNull(object.getLamStations());
        Assert.notEmpty(object.getLamStations());
    }
}
