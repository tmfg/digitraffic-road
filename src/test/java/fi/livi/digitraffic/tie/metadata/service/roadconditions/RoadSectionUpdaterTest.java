package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoadSectionUpdaterTest extends MetadataTest {

    @Autowired
    private RoadSectionUpdater roadSectionUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Test
    @Transactional
    public void updateRoadSectionCoordinatesSucceeds() {
        roadSectionUpdater.updateRoadSections();

        List<ForecastSection> forecastSections = forecastSectionRepository.findAll();

        assertEquals("00001_001_000_0", forecastSections.get(0).getNaturalId());
        assertTrue(forecastSections.get(0).getRoadSectionCoordinates().size() > 3);
        assertEquals(new BigDecimal("24.944"), forecastSections.get(0).getRoadSectionCoordinates().get(0).getLongitude());
        assertEquals(new BigDecimal("60.167"), forecastSections.get(0).getRoadSectionCoordinates().get(0).getLatitude());
    }
}
