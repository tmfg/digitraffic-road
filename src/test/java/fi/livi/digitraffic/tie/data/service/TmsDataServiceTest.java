package fi.livi.digitraffic.tie.data.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.base.MetadataIntegrationTest;
import fi.livi.digitraffic.tie.data.dto.tms.TmsRootDataObjectDto;

public class TmsDataServiceTest extends MetadataIntegrationTest {

    @Autowired
    private TmsDataService tmsDataService;

    @Test
    public void testListAllTmsDataFromNonObsoleteStations()  {
        final TmsRootDataObjectDto object = tmsDataService.findPublicTmsData(false);

        Assert.notNull(object);
        Assert.notNull(object.getDataUpdatedLocalTime());
        Assert.notNull(object.getDataUpdatedUtc());
        Assert.notNull(object.getTmsStations());
        Assert.notEmpty(object.getTmsStations());
    }
}
