package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractTest;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionV2MetadataDao;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2Feature;
import fi.livi.digitraffic.tie.metadata.geojson.forecastsection.ForecastSectionV2FeatureCollection;
import fi.livi.digitraffic.tie.metadata.service.DataStatusService;

public class ForecastSectionV2MetadataUpdaterTest extends AbstractTest {

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionClient forecastSectionClient;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionV2MetadataUpdater forecastSectionMetadataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastSectionV2MetadataService forecastSectionV2MetadataService;

    @Autowired
    private ForecastSectionV2MetadataDao forecastSectionV2MetadataDao;

    @Autowired
    private DataStatusService dataStatusService;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void before() {
        forecastSectionClient = new ForecastSectionClient(restTemplate);
        forecastSectionMetadataUpdater = new ForecastSectionV2MetadataUpdater(forecastSectionClient, forecastSectionRepository, forecastSectionV2MetadataDao,
                                                                              dataStatusService);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void updateForecastSectionV2MetadataSucceeds() throws IOException {

        server.expect(requestTo("/nullroadsV2.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadsV2.json"), MediaType.APPLICATION_JSON));

        forecastSectionMetadataUpdater.updateForecastSectionsV2Metadata();

        final ForecastSectionV2FeatureCollection featureCollection = forecastSectionV2MetadataService.getForecastSectionV2Metadata(false);

        final ForecastSectionV2Feature feature = featureCollection.getFeatures().get(0);

        assertEquals(9473, featureCollection.getFeatures().size());

        assertEquals("00001_001_00000_1_0", feature.getProperties().getNaturalId());

        assertEquals(2, feature.getGeometry().coordinates.get(0).size());
        assertCoordinates(24.9430081, feature.getGeometry().coordinates.get(0).get(0).get(0));
        assertCoordinates(60.1667212, feature.getGeometry().coordinates.get(0).get(0).get(1));
        assertCoordinates(24.9418095, feature.getGeometry().coordinates.get(0).get(1).get(0));
        assertCoordinates(60.1675145, feature.getGeometry().coordinates.get(0).get(1).get(1));
        assertEquals(12, feature.getGeometry().coordinates.get(70).size());
        assertCoordinates(24.9336238, feature.getGeometry().coordinates.get(70).get(0).get(0));
        assertCoordinates(60.1739127, feature.getGeometry().coordinates.get(70).get(0).get(1));

        assertEquals(2, feature.getProperties().getRoadSegments().size());
        assertEquals(0, feature.getProperties().getRoadSegments().get(0).getStartDistance().intValue());
        assertEquals(3264, feature.getProperties().getRoadSegments().get(1).getEndDistance().intValue());
        // TODO: assert linkIds

    }

    private void assertCoordinates(final double expected, final double actual) {
        assertEquals(expected, actual, 0.00000001);
    }

}