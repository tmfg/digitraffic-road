package fi.livi.digitraffic.tie.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import fi.livi.digitraffic.tie.MetadataApplication;
import fi.livi.digitraffic.tie.model.RoadDistrict;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MetadataApplication.class)
@WebAppConfiguration
public class RoadDistrictServiceTest {
    @Autowired
    private RoadDistrictService roadDistrictService;

    @Test
    public void testFindByNaturalIdNotFound() {
        Assert.assertNull(roadDistrictService.findByNaturalId(-1));
    }

    @Test
    public void testFindByNaturalIdFound() {
        final int naturalId = 4;
        final RoadDistrict rd = roadDistrictService.findByNaturalId(naturalId);

        Assert.assertTrue(rd != null);
        Assert.assertSame(naturalId, rd.getNaturalId());
    }

    @Test
    public void testFindByRoadSectionAndRoadNaturalIdNotFound() {
        Assert.assertNull(roadDistrictService.findByRoadSectionAndRoadNaturalId(-1, -1));
    }

    @Test
    public void testFindByRoadSectionAndRoadNaturalIdFound() {
        final int roadNaturalId = 1;
        final int roadSectionNaturalId = 3;
        final RoadDistrict rd = roadDistrictService.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId);

        Assert.assertTrue(rd != null);
    }
}
