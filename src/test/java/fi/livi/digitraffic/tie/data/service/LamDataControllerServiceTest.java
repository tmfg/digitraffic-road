package fi.livi.digitraffic.tie.data.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.data.dto.lam.LamRootDataObjectDto;

public class LamDataControllerServiceTest extends MetadataTest {

    @Autowired
    private LamDataService lamDataService;

    @Test
    public void testListAllLamDataFromNonObsoleteStations()  {
        final LamRootDataObjectDto object = lamDataService.listPublicLamData(false);

        Assert.notNull(object);
        Assert.notNull(object.getDataUptadedLocalTime());
        Assert.notNull(object.getDataUptadedUtc());
        Assert.notNull(object.getLamMeasurements());
        Assert.notEmpty(object.getLamMeasurements());
    }
}
