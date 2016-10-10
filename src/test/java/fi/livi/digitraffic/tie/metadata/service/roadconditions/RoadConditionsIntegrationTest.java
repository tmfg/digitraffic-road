package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import fi.livi.digitraffic.tie.AbstractMetadataTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoadConditionsIntegrationTest extends AbstractMetadataTest {

    @Autowired
    private RoadConditionsClient roadConditionsClient;

    @Test
    public void getRoadSectionCoordinatesSucceeds() {

        List<RoadSectionCoordinatesDto> roadSectionCoordinates = roadConditionsClient.getRoadSections();

        assertTrue(roadSectionCoordinates.size() > 2);
        assertEquals("00001_001_000_0", roadSectionCoordinates.get(0).getNaturalId());
        assertEquals("Vt 1: Helsinki - Kehä III", roadSectionCoordinates.get(0).getName());
        assertEquals(10, roadSectionCoordinates.get(0).getCoordinates().size());
        assertEquals(Arrays.asList(24.944, 60.167), roadSectionCoordinates.get(0).getCoordinates().get(0));
    }
}
