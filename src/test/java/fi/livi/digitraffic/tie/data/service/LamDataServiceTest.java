package fi.livi.digitraffic.tie.data.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.data.model.LamDataObject;

public class LamDataServiceTest extends MetadataTest {
    @Autowired
    private LamDataService lamDataService;

    @Test
    public void testListAllLamDataFromNonObsoleteStations()  {
        final LamDataObject object = lamDataService.listAllLamDataFromNonObsoleteStations();

        Assert.notNull(object);
        Assert.notNull(object.getLocalTime());
        Assert.notNull(object.getUtc());
        Assert.notNull(object.getDynamicLamData());
        Assert.notEmpty(object.getDynamicLamData());
    }
}
