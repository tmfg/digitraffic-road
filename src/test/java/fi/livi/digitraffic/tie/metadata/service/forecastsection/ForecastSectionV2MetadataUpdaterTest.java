package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.io.IOException;
import java.util.List;

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
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;

public class ForecastSectionV2MetadataUpdaterTest extends AbstractTest {

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionClient forecastSectionClient;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionV2MetadataUpdater forecastSectionMetadataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastSectionV2MetadataDao forecastSectionV2MetadataDao;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void before() {
        forecastSectionClient = new ForecastSectionClient(restTemplate);
        forecastSectionMetadataUpdater = new ForecastSectionV2MetadataUpdater(forecastSectionClient, forecastSectionRepository, forecastSectionV2MetadataDao);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void updateForecastSectionV2MetadataSucceeds() throws IOException {

        server.expect(requestTo("/nullroadsV2.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadsV2.json"), MediaType.APPLICATION_JSON));

        forecastSectionMetadataUpdater.updateForecastSectionsV2Metadata();

        final List<ForecastSection> forecastSections = forecastSectionRepository.findDistinctByVersionIsOrderByNaturalIdAsc(2);

        assertEquals(9473, forecastSections.size());

        final ForecastSection forecastSection =
            forecastSections.stream().filter(f -> f.getNaturalId().equals("00005_124_02100_0_0")).findFirst().get();

        assertEquals(2, forecastSection.getForecastSectionCoordinateLists().size());

        assertEquals(2, forecastSection.getForecastSectionCoordinateLists().get(0).getForecastSectionCoordinates().size());
        assertCoordinates(27.0789783, forecastSection.getForecastSectionCoordinateLists().get(0).getForecastSectionCoordinates().get(0).getLongitude().doubleValue());
        assertCoordinates(61.6419140, forecastSection.getForecastSectionCoordinateLists().get(0).getForecastSectionCoordinates().get(0).getLatitude().doubleValue());
        assertCoordinates(27.0793790, forecastSection.getForecastSectionCoordinateLists().get(0).getForecastSectionCoordinates().get(1).getLongitude().doubleValue());
        assertCoordinates(61.6420209, forecastSection.getForecastSectionCoordinateLists().get(0).getForecastSectionCoordinates().get(1).getLatitude().doubleValue());

        assertEquals(2, forecastSection.getForecastSectionCoordinateLists().get(1).getForecastSectionCoordinates().size());
        assertCoordinates(27.0793790, forecastSection.getForecastSectionCoordinateLists().get(1).getForecastSectionCoordinates().get(0).getLongitude().doubleValue());
        assertCoordinates(61.6420209, forecastSection.getForecastSectionCoordinateLists().get(1).getForecastSectionCoordinates().get(0).getLatitude().doubleValue());
        assertCoordinates(27.0797605, forecastSection.getForecastSectionCoordinateLists().get(1).getForecastSectionCoordinates().get(1).getLongitude().doubleValue());
        assertCoordinates(61.6421227, forecastSection.getForecastSectionCoordinateLists().get(1).getForecastSectionCoordinates().get(1).getLatitude().doubleValue());
    }

    private void assertCoordinates(final double expected, final double actual) {
        assertEquals(expected, actual, 0.00000001);
    }

}