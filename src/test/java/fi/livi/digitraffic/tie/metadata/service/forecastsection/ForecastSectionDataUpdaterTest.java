package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import fi.livi.digitraffic.tie.base.MetadataTestBase;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class ForecastSectionDataUpdaterTest  extends MetadataTestBase {

    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Autowired
    private ForecastSectionMetadataUpdater forecastSectionMetadataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Test
    @Transactional
    public void updateForecastSectionWeatherDataSucceeds() {
        forecastSectionMetadataUpdater.updateForecastSectionMetadata();

        forecastSectionDataUpdater.updateForecastSectionWeatherData();

        List<ForecastSection> forecastSections = forecastSectionRepository.findAll(new Sort(Sort.Direction.ASC, "naturalId"));

        assertNotNull(forecastSections);
    }
}