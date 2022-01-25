package fi.livi.digitraffic.tie.service.v1.forecastsection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionFeatureCollection;
import fi.livi.digitraffic.tie.service.DataStatusService;

public class ForecastSectionV1MetadataUpdaterTest extends AbstractDaemonTest {

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastSectionV1MetadataService forecastSectionService;

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

    @Test
    public void updateForecastSectionV1MetadataSucceeds() throws IOException {

        forecastSectionTestHelper.serverExpectMetadata(server, 1);

        forecastSectionMetadataUpdaterMockRealMethods.updateForecastSectionV1Metadata();

        final ForecastSectionFeatureCollection collection = forecastSectionService.findForecastSectionsV1Metadata();
        final ZonedDateTime now = ZonedDateTime.now();
        assertEquals(now.toEpochSecond(), collection.getDataUpdatedTime().toEpochSecond(), 2);
        assertEquals(now.toEpochSecond(), collection.getDataLastCheckedTime().toEpochSecond(), 2);

        assertEquals(10, collection.getFeatures().size());
        assertEquals("00001_001_000_0", collection.getFeatures().get(0).getProperties().getNaturalId());
        assertEquals(10, collection.getFeatures().get(0).getGeometry().getCoordinates().size());
        assertEquals(Double.parseDouble("24.944"), collection.getFeatures().get(0).getGeometry().getCoordinates().get(0).get(0).doubleValue(), 0.01);
        assertEquals(Double.parseDouble("60.167"), collection.getFeatures().get(0).getGeometry().getCoordinates().get(0).get(1).doubleValue(), 0.01);
    }
}
