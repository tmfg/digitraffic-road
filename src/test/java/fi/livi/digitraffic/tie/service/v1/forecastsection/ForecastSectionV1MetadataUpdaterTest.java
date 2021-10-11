package fi.livi.digitraffic.tie.service.v1.forecastsection;

import static fi.livi.digitraffic.tie.TestUtils.readResourceContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
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

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    protected ForecastSectionClient forecastSectionClientMockRealMethods;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    protected ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdaterMockRealMethods;

    @BeforeEach
    public void before() {
        forecastSectionClientMockRealMethods = new ForecastSectionClient(restTemplate, null);
        forecastSectionMetadataUpdaterMockRealMethods = new ForecastSectionV1MetadataUpdater(forecastSectionClientMockRealMethods, forecastSectionRepository, dataStatusService);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void updateForecastSectionV1MetadataSucceeds() throws IOException {

        server.expect(requestTo("/nullroads.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadsV1_min.json"), MediaType.APPLICATION_JSON));

        forecastSectionMetadataUpdaterMockRealMethods.updateForecastSectionV1Metadata();

        final ForecastSectionFeatureCollection collection = forecastSectionService.findForecastSectionsV1Metadata();
        final ZonedDateTime now = ZonedDateTime.now();
        assertEquals(now.toEpochSecond(), collection.getDataUpdatedTime().toEpochSecond(), 2);
        assertEquals(now.toEpochSecond(), collection.getDataLastCheckedTime().toEpochSecond(), 2);

        assertEquals(3, collection.getFeatures().size());
        assertEquals("00009_231_000_0", collection.getFeatures().get(0).getProperties().getNaturalId());
        assertEquals(10, collection.getFeatures().get(0).getGeometry().getCoordinates().size());
        assertEquals(Double.parseDouble("25.601"), collection.getFeatures().get(0).getGeometry().getCoordinates().get(0).get(0).doubleValue(), 0.01);
        assertEquals(Double.parseDouble("62.032"), collection.getFeatures().get(0).getGeometry().getCoordinates().get(0).get(1).doubleValue(), 0.01);
    }
}
