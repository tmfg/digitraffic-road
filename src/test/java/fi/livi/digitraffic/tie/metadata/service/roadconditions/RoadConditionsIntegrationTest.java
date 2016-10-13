package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import fi.livi.digitraffic.tie.AbstractMetadataTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNotNull;
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
        assertEquals(BigDecimal.valueOf(24.944), roadSectionCoordinates.get(0).getCoordinates().get(0).longitude);
        assertEquals(BigDecimal.valueOf(60.167), roadSectionCoordinates.get(0).getCoordinates().get(0).latitude);
    }

    @Test
    public void getRoadSectionWeatherSucceeds() {

        List<RoadSectionWeatherDto> roadSectionWeathers = roadConditionsClient.getRoadSectionWeather();

        List<RoadSectionWeatherDto> weathers = roadSectionWeathers.stream().sorted((a, b) -> a.naturalId.compareTo(b.naturalId)).collect(Collectors.toList());

        assertTrue(weathers.size() > 10);
        assertEquals("00001_001_000_0", weathers.get(0).naturalId);
        assertEquals(5, weathers.get(0).weatherForecasts.size());
        assertNotNull(weathers.get(0).weatherForecasts.get(0).temperature);
    }
}
