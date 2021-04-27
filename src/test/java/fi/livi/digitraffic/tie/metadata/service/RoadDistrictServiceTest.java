package fi.livi.digitraffic.tie.metadata.service;

import org.junit.jupiter.api.Test;import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.model.v1.RoadDistrict;
import fi.livi.digitraffic.tie.service.RoadDistrictService;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoadDistrictServiceTest extends AbstractServiceTest {

    @Autowired
    private RoadDistrictService roadDistrictService;

    @Test
    public void testFindByNaturalIdNotFound() {
        assertNull(roadDistrictService.findByNaturalId(-1));
    }

    @Test
    public void testFindByNaturalIdFound() {
        final int naturalId = 4;
        final RoadDistrict rd = roadDistrictService.findByNaturalId(naturalId);

        assertTrue(rd != null);
        assertSame(naturalId, rd.getNaturalId());
    }

    @Test
    public void testFindByRoadSectionAndRoadNaturalIdNotFound() {
        assertNull(roadDistrictService.findByRoadSectionAndRoadNaturalId(-1, -1));
    }

    @Test
    public void testFindByRoadSectionAndRoadNaturalIdFound() {
        final int roadNaturalId = 1;
        final int roadSectionNaturalId = 3;
        final RoadDistrict rd = roadDistrictService.findByRoadSectionAndRoadNaturalId(roadSectionNaturalId, roadNaturalId);

        assertTrue(rd != null);
    }
}
