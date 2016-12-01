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
    public void testFindPublicTmsData()  {
        final TmsRootDataObjectDto object = tmsDataService.findPublicTmsData(false);
        Assert.notNull(object);
        Assert.notNull(object.getDataUpdatedTime());
        Assert.notNull(object.getTmsStations());
        Assert.notEmpty(object.getTmsStations());
    }

    @Test
    public void testFindPublicTmsDataById()  {
        final TmsRootDataObjectDto object = tmsDataService.findPublicTmsData(23001);
        Assert.notNull(object);
        Assert.notNull(object.getDataUpdatedTime());
        Assert.notNull(object.getTmsStations());
        Assert.notEmpty(object.getTmsStations());
    }
}
