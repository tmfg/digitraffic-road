package fi.livi.digitraffic.tie.metadata.service;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.model.RoadDistrict;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RoadDistrictServiceTest extends MetadataTest {
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
