package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FORECAST_SECTIONS_PATH;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionClient;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionV1MetadataUpdater;

@Transactional()
public class ForecastMetadataControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private RestTemplate restTemplate;


    @Before
    public void initData() throws IOException {
        forecastSectionRepository.deleteAll();
        final ForecastSectionClient forecastSectionClient = new ForecastSectionClient(restTemplate);
        final ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdater =
            new ForecastSectionV1MetadataUpdater(forecastSectionClient, forecastSectionRepository, dataStatusService);
        final MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(requestTo("/nullroads.php"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(MockRestResponseCreators.withSuccess(readResourceContent("classpath:forecastsection/roadsV1_2_roads.json"), MediaType.APPLICATION_JSON));

        forecastSectionMetadataUpdater.updateForecastSectionV1Metadata();
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @Test
    public void testWeatherStationMetadataRestApi() throws Exception {
        final String response =
            mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + FORECAST_SECTIONS_PATH)).andReturn().getResponse().getContentAsString();
        System.out.println(response);

        mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + FORECAST_SECTIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(CONTENT_TYPE))
                .andExpect(jsonPath("$.forecastSections.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.forecastSections.features", hasSize(2)))
                .andExpect(jsonPath("$.forecastSections.features[0].geometry.type", is("LineString")))
                .andExpect(jsonPath("$.forecastSections.features[0].geometry.coordinates", hasSize(greaterThan(1))))
                .andExpect(jsonPath("$.forecastSections.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.forecastSections.features[0].id", isA(Integer.class)))
                ;
    }
}
