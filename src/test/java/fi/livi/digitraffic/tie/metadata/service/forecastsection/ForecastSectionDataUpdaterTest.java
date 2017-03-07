package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;

public class ForecastSectionDataUpdaterTest extends AbstractTest {

    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Autowired
    private ForecastSectionMetadataUpdater forecastSectionMetadataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Test
    public void updateForecastSectionWeatherDataSucceeds() {
        forecastSectionMetadataUpdater.updateForecastSectionMetadata();

        forecastSectionDataUpdater.updateForecastSectionWeatherData();

        List<ForecastSection> forecastSections = forecastSectionRepository.findAll(new Sort(Sort.Direction.ASC, "naturalId"));

        assertNotNull(forecastSections);
    }
}