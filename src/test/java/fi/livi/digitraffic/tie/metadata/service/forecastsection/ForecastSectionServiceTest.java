package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;

public class ForecastSectionServiceTest extends AbstractTest {

    @Autowired
    private ForecastSectionService forecastSectionService;

    @Autowired
    private ForecastSectionMetadataUpdater forecastSectionMetadataUpdater;

    @Test
    public void findAllForecastSectionsSucceeds() {

        forecastSectionMetadataUpdater.updateForecastSectionMetadata();

        ForecastSectionFeatureCollection forecastSections = forecastSectionService.findAllForecastSections();

        assertTrue(forecastSections.getFeatures().size() > 250);
        ForecastSectionFeature firstFeature = forecastSections.getFeatures().get(0);

        assertEquals("00001_001_000_0", firstFeature.getProperties().getNaturalId());
        assertEquals("Vt 1: Helsinki - Kehä III", firstFeature.getProperties().getDescription());
        assertTrue(firstFeature.getGeometry().getCoordinates().size() >= 8);
        assertTrue(firstFeature.getGeometry().getCoordinates().stream().allMatch(c -> c.size() == 2));
        assertNotNull(forecastSections);
    }
}
