package fi.livi.digitraffic.tie.metadata.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V1_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.FORECAST_SECTIONS_PATH;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionClient;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionTestHelper;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionV1MetadataUpdater;

public class ForecastMetadataControllerRestWebTest extends AbstractRestWebTest {

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ForecastSectionTestHelper forecastSectionTestHelper;

    @BeforeEach
    public void initData() throws IOException {
        forecastSectionRepository.deleteAll();
        final ForecastSectionClient forecastSectionClient = forecastSectionTestHelper.createForecastSectionClient();// new ForecastSectionClient(restTemplate, objectMapper, null, null, null, null);

        final ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdater =
            new ForecastSectionV1MetadataUpdater(forecastSectionClient, forecastSectionRepository, dataStatusService);
        final MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        forecastSectionTestHelper.serverExpectMetadata(server,1);

        forecastSectionMetadataUpdater.updateForecastSectionV1Metadata();
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @Test
    public void testSectionsMetadataRestApi() throws Exception {
        final String response =
            mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + FORECAST_SECTIONS_PATH)).andReturn().getResponse().getContentAsString();

        mockMvc.perform(get(API_V1_BASE_PATH + API_METADATA_PART_PATH + FORECAST_SECTIONS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
                .andExpect(jsonPath("$.forecastSections.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.forecastSections.features", hasSize(10)))
                .andExpect(jsonPath("$.forecastSections.features[0].geometry.type", is("LineString")))
                .andExpect(jsonPath("$.forecastSections.features[0].geometry.coordinates", hasSize(greaterThan(1))))
                .andExpect(jsonPath("$.forecastSections.features[0].type", is("Feature")))
                .andExpect(jsonPath("$.forecastSections.features[0].id", isA(Integer.class)))
                ;
    }
}
