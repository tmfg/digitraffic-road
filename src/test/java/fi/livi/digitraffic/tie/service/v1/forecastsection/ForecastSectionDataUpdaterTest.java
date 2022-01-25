package fi.livi.digitraffic.tie.service.v1.forecastsection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.v2.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.dto.v1.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.v1.forecastsection.RoadCondition;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v1.ForecastSectionDataService;
import fi.livi.digitraffic.tie.service.v2.forecastsection.V2ForecastSectionMetadataUpdater;

public class ForecastSectionDataUpdaterTest extends AbstractDaemonTest {

    @Autowired
    private ForecastSectionClient forecastSectionClient;

    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastSectionDataService forecastSectionDataService;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    private V2ForecastSectionMetadataUpdater forecastSectionMetadataUpdaterV2;
    private ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdaterV1;

    @Autowired
    private V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao;

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private ForecastSectionTestHelper forecastSectionTestHelper;

    @BeforeEach
    public void before() throws IOException {
        server = MockRestServiceServer.createServer(restTemplate);
        forecastSectionMetadataUpdaterV2 =
            new V2ForecastSectionMetadataUpdater(forecastSectionClient, forecastSectionRepository, v2ForecastSectionMetadataDao, dataStatusService);
        forecastSectionMetadataUpdaterV1 =
            new ForecastSectionV1MetadataUpdater(forecastSectionClient, forecastSectionRepository, dataStatusService);
    }

    @Test
    public void updateForecastSectionV1DataSucceeds() {
        forecastSectionTestHelper.serverExpectMetadata(server, 1);
        forecastSectionTestHelper.serverExpectData(server, 1);

        forecastSectionMetadataUpdaterV1.updateForecastSectionV1Metadata();

        final Instant dataUpdated = forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V1);

        final Instant lastUpdated = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_WEATHER_DATA).toInstant();

        assertEquals(dataUpdated, lastUpdated);

        final ForecastSectionWeatherRootDto data = forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V1, false,
                                                                                                            null,
                                                                                                            null, null,
                                                                                                            null, null,
                                                                                                            null);

        assertEquals(3, data.weatherData.size());
        assertEquals("00001_001_000_0", data.weatherData.get(0).naturalId);
        assertEquals(5, data.weatherData.get(0).roadConditions.size());
        assertEquals("0h", data.weatherData.get(0).roadConditions.get(0).getForecastName());
        assertEquals("2h", data.weatherData.get(0).roadConditions.get(1).getForecastName());
        assertEquals("4h", data.weatherData.get(0).roadConditions.get(2).getForecastName());
        assertEquals("6h", data.weatherData.get(0).roadConditions.get(3).getForecastName());
        assertEquals("12h", data.weatherData.get(0).roadConditions.get(4).getForecastName());
    }

    @Test
    public void updateForecastSectionV2DataSucceeds() throws IOException {

        forecastSectionRepository.deleteAllInBatch();
        forecastSectionTestHelper.serverExpectMetadata(server, 2);
        forecastSectionTestHelper.serverExpectData(server, 2);

        final Instant metadataUpdated = forecastSectionMetadataUpdaterV2.updateForecastSectionsV2Metadata();

        final Instant dataUpdated = forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V2);

        final Instant metadataLastUpdated = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_V2_METADATA).toInstant();
        final Instant dataLastUpdated = dataStatusService.findDataUpdatedTime(DataType.FORECAST_SECTION_V2_WEATHER_DATA).toInstant();

        assertEquals(metadataUpdated, metadataLastUpdated);
        assertEquals(dataUpdated, dataLastUpdated);

        final ForecastSectionWeatherRootDto data = forecastSectionDataService.getForecastSectionWeatherData(ForecastSectionApiVersion.V2, false,
                                                                                                            null,
                                                                                                            null, null, null, null,
                                                                                                            null);

        assertNotNull(data);
        assertEquals("00003_226_00000_0_0", data.weatherData.get(0).naturalId);
        assertEquals(5, data.weatherData.get(0).roadConditions.size());
        assertEquals("0h", data.weatherData.get(0).roadConditions.get(0).getForecastName());
        assertEquals(ZonedDateTime.parse("2018-11-14T14:00+02:00[Europe/Helsinki]").toInstant(), data.weatherData.get(0).roadConditions.get(0).getTime().toInstant());
        assertEquals("+4.2", data.weatherData.get(0).roadConditions.get(0).getTemperature());
        assertEquals("12h", data.weatherData.get(0).roadConditions.get(4).getForecastName());
        assertEquals(RoadCondition.MOIST, data.weatherData.get(0).roadConditions.get(4).getForecastConditionReason().getRoadCondition());

        assertEquals("00009_216_03050_0_0", data.weatherData.get(1).naturalId);
        assertEquals(5, data.weatherData.get(1).roadConditions.size());
        assertEquals("2h", data.weatherData.get(0).roadConditions.get(1).getForecastName());
        assertEquals("+3", data.weatherData.get(0).roadConditions.get(4).getRoadTemperature());
    }
}
