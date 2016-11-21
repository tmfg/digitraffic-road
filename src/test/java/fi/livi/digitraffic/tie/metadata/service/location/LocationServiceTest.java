package fi.livi.digitraffic.tie.metadata.service.location;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.base.AbstractTestBase;

public class LocationServiceTest extends AbstractTestBase {
    @Autowired
    private LocationService locationService;

    @Test
    public void testFindLocation() {
        Assert.assertNotNull(locationService.findLocation(12316));
    }
}
