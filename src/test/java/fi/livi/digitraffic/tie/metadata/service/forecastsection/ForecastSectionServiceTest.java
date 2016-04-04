package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.MetadataTest;
import fi.livi.digitraffic.tie.metadata.model.ForecastSection;

public class ForecastSectionServiceTest extends MetadataTest {
    @Autowired
    private ForecastSectionService forecastSectionService;

    @Test
    public void test() {
        final List<ForecastSection> sections = forecastSectionService.findAllForecastSections();
        Assert.assertEquals(266, sections.size());
    }
}
