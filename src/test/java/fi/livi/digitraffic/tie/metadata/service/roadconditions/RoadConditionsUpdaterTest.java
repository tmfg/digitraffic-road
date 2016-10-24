package fi.livi.digitraffic.tie.metadata.service.roadconditions;

import fi.livi.digitraffic.tie.AbstractMetadataTest;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoadConditionsUpdaterTest extends AbstractMetadataTest {

    @Autowired
    private RoadConditionsUpdater roadConditionsUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Test
    @Transactional
    public void updateForecastSectionCoordinatesSucceeds() {
        roadConditionsUpdater.updateForecastSectionCoordinates();

        List<ForecastSection> forecastSections = forecastSectionRepository.findAll(new Sort(Sort.Direction.ASC, "naturalId"));

        assertEquals("00001_001_000_0", forecastSections.get(0).getNaturalId());
        assertTrue(forecastSections.get(0).getForecastSectionCoordinates().size() > 3);
        assertEquals(new BigDecimal("24.944"), forecastSections.get(0).getForecastSectionCoordinates().get(0).getLongitude());
        assertEquals(new BigDecimal("60.167"), forecastSections.get(0).getForecastSectionCoordinates().get(0).getLatitude());
    }
}
