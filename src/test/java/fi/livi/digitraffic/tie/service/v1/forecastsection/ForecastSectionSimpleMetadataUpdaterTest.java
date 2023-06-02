package fi.livi.digitraffic.tie.service.v1.forecastsection;

import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MAX_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.X_MIN_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MAX_DOUBLE;
import static fi.livi.digitraffic.tie.controller.ControllerConstants.Y_MIN_DOUBLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dto.weather.v1.forecast.ForecastSectionFeatureCollectionSimpleV1;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.weather.v1.forecast.ForecastWebDataServiceV1;

public class ForecastSectionSimpleMetadataUpdaterTest extends AbstractDaemonTest {

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastWebDataServiceV1 forecastWebDataServiceV1;

    @Autowired
    private DataStatusService dataStatusService;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    protected ForecastSectionClient forecastSectionClient;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    protected ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdaterMockRealMethods;

    @Autowired
    private ForecastSectionTestHelper forecastSectionTestHelper;

    @BeforeEach
    public void before() {
        forecastSectionMetadataUpdaterMockRealMethods =
            new ForecastSectionV1MetadataUpdater(forecastSectionClient, forecastSectionRepository, dataStatusService);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @AfterEach
    public void after() {
        forecastSectionRepository.deleteAllInBatch();
    }

    @Test
    public void updateForecastSectionV1MetadataSucceeds() {
        forecastSectionTestHelper.serverExpectMetadata(server, 1);

        forecastSectionMetadataUpdaterMockRealMethods.updateForecastSectionV1Metadata();

        final ForecastSectionFeatureCollectionSimpleV1 collection =
            forecastWebDataServiceV1.findSimpleForecastSections(false, null,
                X_MIN_DOUBLE, Y_MIN_DOUBLE, X_MAX_DOUBLE, Y_MAX_DOUBLE);

        final ZonedDateTime now = ZonedDateTime.now();
        assertEquals(now.toEpochSecond(), collection.dataUpdatedTime.getEpochSecond(), 2);

        assertEquals(10, collection.getFeatures().size());
        assertEquals("00001_001_000_0", collection.getFeatures().get(0).getProperties().id);
        assertEquals(10, collection.getFeatures().get(0).getGeometry().getCoordinates().size());
        assertEquals(Double.parseDouble("24.944"), collection.getFeatures().get(0).getGeometry().getCoordinates().get(0).get(0), 0.01);
        assertEquals(Double.parseDouble("60.167"), collection.getFeatures().get(0).getGeometry().getCoordinates().get(0).get(1), 0.01);
    }
}
