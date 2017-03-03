package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;

public class ForecastSectionMetadataUpdaterTest extends AbstractTest {

    @Autowired
    private ForecastSectionMetadataUpdater forecastSectionMetadataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Test
    public void updateForecastSectionCoordinatesSucceeds() {
        forecastSectionMetadataUpdater.updateForecastSectionMetadata();

        List<ForecastSection> forecastSections = forecastSectionRepository.findAll(new Sort(Sort.Direction.ASC, "naturalId"));

        assertTrue(forecastSections.size() > 250);
        assertEquals("00001_001_000_0", forecastSections.get(0).getNaturalId());
        assertTrue(forecastSections.get(0).getForecastSectionCoordinates().size() > 3);
        assertEquals(new BigDecimal("24.944"), forecastSections.get(0).getForecastSectionCoordinates().get(0).getLongitude());
        assertEquals(new BigDecimal("60.167"), forecastSections.get(0).getForecastSectionCoordinates().get(0).getLatitude());
    }
}
