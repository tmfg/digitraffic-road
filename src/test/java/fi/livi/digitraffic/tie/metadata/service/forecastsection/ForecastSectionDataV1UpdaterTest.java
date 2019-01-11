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
import fi.livi.digitraffic.tie.data.dto.forecast.ForecastSectionWeatherRootDto;
import fi.livi.digitraffic.tie.data.service.ForecastSectionDataService;
import fi.livi.digitraffic.tie.metadata.dao.ForecastSectionRepository;

public class ForecastSectionDataV1UpdaterTest extends AbstractTest {

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionClient forecastSectionClient;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private ForecastSectionV1DataUpdater forecastSectionDataUpdater;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private ForecastSectionDataService forecastSectionDataService;

    private MockRestServiceServer server;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void before() {
        forecastSectionClient = new ForecastSectionClient(restTemplate);
        forecastSectionDataUpdater = new ForecastSectionV1DataUpdater(forecastSectionClient, forecastSectionRepository);
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void updateForecastSectionV1DataSucceeds() throws IOException {

        server.expect(requestTo("/nullroadConditionsV1-json.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadConditionsV1.json"), MediaType.APPLICATION_JSON));

        forecastSectionDataUpdater.updateForecastSectionWeatherData();

        final ForecastSectionWeatherRootDto data = forecastSectionDataService.getForecastSectionWeatherData(false);

        assertEquals(277, data.weatherData.size());
        assertEquals("00009_303_000_0", data.weatherData.get(0).naturalId);
        assertEquals(5, data.weatherData.get(0).roadConditions.size());
        assertEquals("0h", data.weatherData.get(0).roadConditions.get(0).getForecastName());
        assertEquals("2h", data.weatherData.get(0).roadConditions.get(1).getForecastName());
        assertEquals("4h", data.weatherData.get(0).roadConditions.get(2).getForecastName());
        assertEquals("6h", data.weatherData.get(0).roadConditions.get(3).getForecastName());
        assertEquals("12h", data.weatherData.get(0).roadConditions.get(4).getForecastName());
    }
}
