package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;

public class ForecastSectionDataUpdaterTest extends AbstractTest {

    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Autowired
    private ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Test
    public void updateForecastSectionWeatherDataSucceeds() {
        forecastSectionMetadataUpdater.updateForecastSectionV1Metadata();
        forecastSectionDataUpdater.updateForecastSectionWeatherData();

        final List<ForecastSection> forecastSections = forecastSectionRepository.findDistinctByVersionIsOrderByNaturalIdAsc(1);

        assertNotNull(forecastSections);
    }
}