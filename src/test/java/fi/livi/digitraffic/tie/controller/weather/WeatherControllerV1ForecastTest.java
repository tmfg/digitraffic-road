package fi.livi.digitraffic.tie.controller.weather;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.v1.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.v2.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.RestTemplateGzipService;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionClient;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionTestHelper;
import fi.livi.digitraffic.tie.service.v1.forecastsection.ForecastSectionV1MetadataUpdater;
import fi.livi.digitraffic.tie.service.v2.forecastsection.V2ForecastSectionMetadataUpdater;

public class WeatherControllerV1ForecastTest extends AbstractRestWebTest {

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao;
    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private RestTemplate restTemplate;



    @BeforeEach
    public void initData() throws IOException {
        if (!isBeanRegistered(RestTemplateGzipService.class)) {
            final RestTemplateGzipService restTemplateGzipService = beanFactory.createBean(RestTemplateGzipService.class);
            beanFactory.registerSingleton(restTemplateGzipService.getClass().getCanonicalName(), restTemplateGzipService);
        }
        final ForecastSectionTestHelper forecastSectionTestHelper =
            isBeanRegistered(ForecastSectionTestHelper.class) ?
                beanFactory.getBean(ForecastSectionTestHelper.class) :
                beanFactory.createBean(ForecastSectionTestHelper.class);

        final ForecastSectionClient forecastSectionClient = forecastSectionTestHelper.createForecastSectionClient();

        final ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdater =
            new ForecastSectionV1MetadataUpdater(forecastSectionClient, forecastSectionRepository, dataStatusService);
        final V2ForecastSectionMetadataUpdater v2ForecastSectionMetadataUpdater =
            new V2ForecastSectionMetadataUpdater(forecastSectionClient, forecastSectionRepository,
                                                 v2ForecastSectionMetadataDao, dataStatusService);

        final MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);

        forecastSectionTestHelper.serverExpectMetadata(server,1);
        forecastSectionTestHelper.serverExpectMetadata(server,2);

        forecastSectionMetadataUpdater.updateForecastSectionV1Metadata();
        v2ForecastSectionMetadataUpdater.updateForecastSectionsV2Metadata();
        TestUtils.commitAndEndTransactionAndStartNew();
    }

    @AfterEach
    public void after() {
        TestUtils.commitAndEndTransactionAndStartNew();
        forecastSectionRepository.deleteAllInBatch();
        TestUtils.commitAndEndTransactionAndStartNew();
    }

    @Test
    public void forecastSectionsSimple() throws Exception {
        final String response =
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_BETA + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE)).andReturn().getResponse().getContentAsString();
        System.out.println(response);

        mockMvc.perform(get(WeatherControllerV1.API_WEATHER_BETA + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.type", is("FeatureCollection")))
            .andExpect(jsonPath("$.features", hasSize(10)))
            .andExpect(jsonPath("$.features[0].geometry.type", is("LineString")))
            .andExpect(jsonPath("$.features[0].geometry.coordinates", hasSize(greaterThan(1))))
            .andExpect(jsonPath("$.features[0].type", is("Feature")))
            .andExpect(jsonPath("$.features[0].id", isA(String.class)))
            .andExpect(jsonPath("$.features[0].properties.id", isA(String.class)))
            .andExpect(jsonPath("$.features[0].properties.description", isA(String.class)))
            .andExpect(jsonPath("$.features[0].properties.roadSectionNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.features[0].properties.roadNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.features[0].properties.roadSectionVersionNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.features[0].properties.dataUpdatedTime", isA(String.class)))
        ;
    }

    @Test
    public void forecastSections() throws Exception {
        final String response =
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_BETA + WeatherControllerV1.FORECAST_SECTIONS)).andReturn().getResponse().getContentAsString();
        System.out.println(response);

        mockMvc.perform(get(WeatherControllerV1.API_WEATHER_BETA + WeatherControllerV1.FORECAST_SECTIONS))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.type", is("FeatureCollection")))
            .andExpect(jsonPath("$.features", hasSize(3)))
            .andExpect(jsonPath("$.features[0].geometry.type", is("MultiLineString")))
            .andExpect(jsonPath("$.features[0].geometry.coordinates", hasSize(greaterThan(1))))
            .andExpect(jsonPath("$.features[0].type", is("Feature")))
            .andExpect(jsonPath("$.features[0].id", isA(String.class)))

            .andExpect(jsonPath("$.features[0].properties.id", isA(String.class)))
            .andExpect(jsonPath("$.features[0].properties.description", isA(String.class)))
            .andExpect(jsonPath("$.features[0].properties.roadSectionNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.features[0].properties.roadNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.features[0].properties.length", isA(Integer.class)))
            .andExpect(jsonPath("$.features[0].properties.linkIds", hasSize(greaterThan(1))))
            .andExpect(jsonPath("$.features[0].properties.dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.features[0].properties.roadSegments", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.features[0].properties.roadSegments[0].startDistance", isA(Integer.class)))
            .andExpect(jsonPath("$.features[0].properties.roadSegments[0].endDistance", isA(Integer.class)))
        ;
    }
}
