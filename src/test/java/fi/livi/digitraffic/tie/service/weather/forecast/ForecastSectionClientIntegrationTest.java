package fi.livi.digitraffic.tie.service.weather.forecast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import fi.livi.digitraffic.tie.AbstractServiceTest;
import fi.livi.digitraffic.tie.dto.weather.forecast.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionCoordinatesDto;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionDataDto;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionV2Dto;
import fi.livi.digitraffic.tie.service.RestTemplateGzipService;

// Tests on this class are disabled so import won't affect test performance
@Disabled("For manual integration testing")
@Import({ ForecastSectionClient.class, RestTemplateGzipService.class })
public class ForecastSectionClientIntegrationTest extends AbstractServiceTest {

    @Autowired
    private ForecastSectionClient forecastSectionClient;

    @Test
    public void getForecastSectionV1MetadataSucceeds() {
        List<ForecastSectionCoordinatesDto> forecastSectionCoordinates = forecastSectionClient.getForecastSectionV1Metadata();

        assertTrue(forecastSectionCoordinates.size() > 2);
        assertEquals("00001_001_000_0", forecastSectionCoordinates.get(0).getNaturalId());
        assertEquals("Vt 1: Helsinki - Kehä III", forecastSectionCoordinates.get(0).getName());
        assertEquals(10, forecastSectionCoordinates.get(0).getCoordinates().size());
        assertEquals(BigDecimal.valueOf(24.944), forecastSectionCoordinates.get(0).getCoordinates().get(0).longitude);
        assertEquals(BigDecimal.valueOf(60.167), forecastSectionCoordinates.get(0).getCoordinates().get(0).latitude);
    }

    @Test
    public void getForecastSectionV2MetadataSucceeds() {
        final ForecastSectionV2Dto forecastSectionV2Metadata = forecastSectionClient.getForecastSectionV2Metadata();

        assertNotNull(forecastSectionV2Metadata);
        assertNotNull(forecastSectionV2Metadata.getFeatures());
        assertNotNull(forecastSectionV2Metadata.getFeatures().get(0));
        assertNotNull(forecastSectionV2Metadata.getFeatures().get(0).getGeometry());
        assertNotNull(forecastSectionV2Metadata.getFeatures().get(0).getProperties().getId());
        assertNotNull(forecastSectionV2Metadata.getFeatures().get(0).getProperties().getRoadNumber());
    }

    @Test
    public void getRoadConditionsV2Succeeds() {
        final ForecastSectionDataDto roadConditions = forecastSectionClient.getRoadConditions(ForecastSectionApiVersion.V2.getVersion());

        assertTrue(roadConditions.forecastSectionWeatherList.size() > 250);
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0));
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast);
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast.get(0));
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast.get(0).time);
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast.get(0).weatherSymbol);
    }

    @Test
    public void getRoadConditionsV1Succeeds() {
        final   ForecastSectionDataDto roadConditions = forecastSectionClient.getRoadConditions(ForecastSectionApiVersion.V1.getVersion());

        assertTrue(roadConditions.forecastSectionWeatherList.size() > 250);
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0));
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast);
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast.get(0));
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast.get(0).time);
        assertNotNull(roadConditions.forecastSectionWeatherList.get(0).forecast.get(0).weatherSymbol);
    }
}