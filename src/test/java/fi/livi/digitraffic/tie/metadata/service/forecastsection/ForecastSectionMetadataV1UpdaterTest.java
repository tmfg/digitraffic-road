package fi.livi.digitraffic.tie.metadata.service.forecastsection;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

import java.io.IOException;
import java.math.BigDecimal;
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
import fi.livi.digitraffic.tie.metadata.model.forecastsection.ForecastSection;

public class ForecastSectionMetadataV1UpdaterTest extends AbstractTest {

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionClient forecastSectionClient;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void before() {
        forecastSectionClient = new ForecastSectionClient(restTemplate);
        forecastSectionMetadataUpdater = new ForecastSectionV1MetadataUpdater(forecastSectionClient, forecastSectionRepository);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void updateForecastSectionV1MetadataSucceeds() throws IOException {

        server.expect(requestTo("/nullroads.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadsV1.json"), MediaType.APPLICATION_JSON));

        forecastSectionMetadataUpdater.updateForecastSectionV1Metadata();

        final List<ForecastSection> forecastSections = forecastSectionRepository.findDistinctByVersionIsOrderByNaturalIdAsc(1);

        assertEquals(277, forecastSections.size());
        assertEquals("00001_001_000_0", forecastSections.get(0).getNaturalId());
        assertEquals(10, forecastSections.get(0).getForecastSectionCoordinateLists().get(0).getForecastSectionCoordinates().size());
        assertEquals(new BigDecimal("24.944"), forecastSections.get(0).getForecastSectionCoordinateLists().get(0).getForecastSectionCoordinates().get(0).getLongitude());
        assertEquals(new BigDecimal("60.167"), forecastSections.get(0).getForecastSectionCoordinateLists().get(0).getForecastSectionCoordinates().get(0).getLatitude());
    }
}
