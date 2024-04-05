package fi.livi.digitraffic.tie.service.weather.forecast;

import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MAX_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MIN_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MAX_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MIN_DOUBLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.weather.forecast.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.dto.weather.forecast.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.dto.weather.forecast.v1.ForecastSectionsWeatherDtoV1;
import fi.livi.digitraffic.tie.model.DataType;
import fi.livi.digitraffic.tie.model.weather.forecast.RoadCondition;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.weather.forecast.v1.ForecastWebDataServiceV1;

public class ForecastSectionDataUpdaterTest extends AbstractDaemonTest {

    @Autowired
    private ForecastSectionClient forecastSectionClient;

    @Autowired
    private ForecastSectionDataUpdater forecastSectionDataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastWebDataServiceV1 forecastSectionDataService;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    private ForecastSectionV2MetadataUpdater forecastSectionMetadataUpdaterV2;
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
            new ForecastSectionV2MetadataUpdater(forecastSectionClient, forecastSectionRepository, v2ForecastSectionMetadataDao, dataStatusService);
        forecastSectionMetadataUpdaterV1 =
            new ForecastSectionV1MetadataUpdater(forecastSectionClient, forecastSectionRepository, dataStatusService);
    }

    @AfterEach
    public void after() {
        forecastSectionRepository.deleteAllInBatch();
    }

    @Test
    public void updateForecastSectionV1DataSucceeds() {
        forecastSectionTestHelper.serverExpectMetadata(server, 1);
        forecastSectionTestHelper.serverExpectData(server, 1);

        forecastSectionMetadataUpdaterV1.updateForecastSectionV1Metadata();

        final Instant dataUpdated = forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V1);

        final Instant lastUpdated = dataStatusService.findDataUpdatedInstant(DataType.FORECAST_SECTION_WEATHER_DATA);

        assertEquals(dataUpdated, lastUpdated);

        final ForecastSectionsWeatherDtoV1 data =
            forecastSectionDataService.getForecastSectionWeatherData(
                ForecastSectionApiVersion.V1, false, null,
                X_MIN_DOUBLE, Y_MIN_DOUBLE, X_MAX_DOUBLE, Y_MAX_DOUBLE);

        assertEquals(4, data.forecastSections.size());
        assertEquals("00001_001_000_0", data.forecastSections.get(0).id);
        assertEquals(5, data.forecastSections.get(0).forecasts.size());
        assertEquals("0h", data.forecastSections.get(0).forecasts.get(0).getForecastName());
        assertEquals("2h", data.forecastSections.get(0).forecasts.get(1).getForecastName());
        assertEquals("4h", data.forecastSections.get(0).forecasts.get(2).getForecastName());
        assertEquals("6h", data.forecastSections.get(0).forecasts.get(3).getForecastName());
        assertEquals("12h", data.forecastSections.get(0).forecasts.get(4).getForecastName());
    }

    @Test
    public void updateForecastSectionV2DataSucceeds() {
        forecastSectionTestHelper.serverExpectMetadata(server, 2);
        forecastSectionTestHelper.serverExpectData(server, 2);

        final Instant dataUpdated = forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V2);
        final Instant dataLastUpdated = dataStatusService.findDataUpdatedInstant(DataType.FORECAST_SECTION_V2_WEATHER_DATA);

        assertEquals(dataUpdated, dataLastUpdated);

        final ForecastSectionsWeatherDtoV1 data =
            forecastSectionDataService.getForecastSectionWeatherData(
                ForecastSectionApiVersion.V2, false, null,
                X_MIN_DOUBLE, Y_MIN_DOUBLE, X_MAX_DOUBLE, Y_MAX_DOUBLE);

        assertNotNull(data);
        assertEquals("00004_229_00307_1_0", data.forecastSections.get(0).id);
        assertEquals(5, data.forecastSections.get(0).forecasts.size());
        assertEquals("0h", data.forecastSections.get(0).forecasts.get(0).getForecastName());
        assertEquals(ForecastSectionTestHelper.TIMES[0], data.forecastSections.get(0).forecasts.get(0).getTime().toString());
        assertEquals(5.7, data.forecastSections.get(0).forecasts.get(0).getTemperature());
        assertEquals("12h", data.forecastSections.get(0).forecasts.get(4).getForecastName());
        assertEquals(RoadCondition.WET, data.forecastSections.get(0).forecasts.get(4).getForecastConditionReason().roadCondition);

        assertEquals("00409_001_01796_0_0", data.forecastSections.get(1).id);
        assertEquals(5, data.forecastSections.get(1).forecasts.size());
        assertEquals("2h", data.forecastSections.get(1).forecasts.get(1).getForecastName());
        assertEquals(-1, data.forecastSections.get(1).forecasts.get(1).getTemperature());
        assertEquals(6, data.forecastSections.get(1).forecasts.get(4).getRoadTemperature());
    }
}
