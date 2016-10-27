package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import fi.livi.digitraffic.tie.base.MetadataTestBase;
import fi.livi.digitraffic.tie.metadata.geojson.roadconditions.ForecastSectionFeature;
import fi.livi.digitraffic.tie.metadata.geojson.roadconditions.ForecastSectionFeatureCollection;
import fi.livi.digitraffic.tie.metadata.service.roadconditions.RoadConditionsUpdater;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class ForecastSectionServiceTest extends MetadataTestBase {

    @Autowired
    private ForecastSectionService forecastSectionService;

    @Autowired
    private RoadConditionsUpdater roadConditionsUpdater;

    @Test
    public void findAllForecastSectionsSucceeds() {

        roadConditionsUpdater.updateForecastSectionCoordinates();

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
