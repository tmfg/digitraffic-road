package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import fi.livi.digitraffic.tie.AbstractMetadataTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoadConditionsIntegrationTest extends AbstractMetadataTest {

    @Autowired
    private RoadConditionsClient roadConditionsClient;

    @Test
    public void getForecastSectionCoordinatesSucceeds() {

        List<ForecastSectionCoordinatesDto> forecastSectionCoordinates = roadConditionsClient.getForecastSectionMetadata();

        assertTrue(forecastSectionCoordinates.size() > 2);
        assertEquals("00001_001_000_0", forecastSectionCoordinates.get(0).getNaturalId());
        assertEquals("Vt 1: Helsinki - Kehä III", forecastSectionCoordinates.get(0).getName());
        assertEquals(10, forecastSectionCoordinates.get(0).getCoordinates().size());
        assertEquals(BigDecimal.valueOf(24.944), forecastSectionCoordinates.get(0).getCoordinates().get(0).longitude);
        assertEquals(BigDecimal.valueOf(60.167), forecastSectionCoordinates.get(0).getCoordinates().get(0).latitude);
    }
}
