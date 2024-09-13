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

import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.weather.forecast.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.dto.weather.forecast.ForecastSectionApiVersion;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionClient;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionDataUpdater;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionTestHelper;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionV1MetadataUpdater;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionV2MetadataUpdater;

public class WeatherControllerV1ForecastTest extends AbstractRestWebTest {

    private static final Logger log = LoggerFactory.getLogger(WeatherControllerV1ForecastTest.class);

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao;
    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private ForecastSectionTestHelper forecastSectionTestHelper;
    private MockWebServer server;

    @BeforeEach
    public void initData() throws IOException {
        final StopWatch start = StopWatch.createStarted();

        server = new MockWebServer();

        final ForecastSectionClient forecastSectionClient = forecastSectionTestHelper.createForecastSectionClient(server);

        final ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdater =
            new ForecastSectionV1MetadataUpdater(forecastSectionClient, forecastSectionRepository, dataStatusService);
        final ForecastSectionV2MetadataUpdater forecastSectionV2MetadataUpdater =
            new ForecastSectionV2MetadataUpdater(forecastSectionClient, forecastSectionRepository,
                                                 v2ForecastSectionMetadataDao, dataStatusService);

        final ForecastSectionDataUpdater forecastSectionDataUpdater =
            new ForecastSectionDataUpdater(forecastSectionClient, forecastSectionRepository, dataStatusService);

        forecastSectionTestHelper.serveGzippedMetadata(server,1);
        forecastSectionTestHelper.serveGzippedMetadata(server,2);

        forecastSectionTestHelper.serveGzippedData(server,1);
        forecastSectionTestHelper.serveGzippedData(server,2);

        forecastSectionMetadataUpdater.updateForecastSectionV1Metadata();
        forecastSectionV2MetadataUpdater.updateForecastSectionsV2Metadata();
        forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V1);
        forecastSectionDataUpdater.updateForecastSectionWeatherData(ForecastSectionApiVersion.V2);
        entityManager.flush();
        entityManager.clear();
        log.info("Init data tookMs={}", start.getTime());
    }

    @AfterEach
    public void after() throws IOException {
        server.close();
    }

    @Test
    public void forecastSectionsSimple() throws Exception {
        logDebugResponse(mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE)))
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
    public void forecastSectionsSimpleByRoadNumber() throws Exception {
        logDebugResponse(mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + "?roadNumber=1")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.type", is("FeatureCollection")))
            .andExpect(jsonPath("$.features", hasSize(7)))
            .andExpect(jsonPath("$.features[0].properties.roadNumber", is(1)))
        ;
    }

    @Test
    public void forecastSectionsSimpleById() throws Exception {
        final String id = "00001_001_000_0";
        logDebugResponse(
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + "/" + id)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.geometry.type", is("LineString")))
            .andExpect(jsonPath("$.geometry.coordinates", hasSize(greaterThan(1))))
            .andExpect(jsonPath("$.type", is("Feature")))
            .andExpect(jsonPath("$.id", is(id)))
            .andExpect(jsonPath("$.properties.id", is(id)))
            .andExpect(jsonPath("$.properties.description", isA(String.class)))
            .andExpect(jsonPath("$.properties.roadSectionNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.properties.roadNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.properties.roadSectionVersionNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.properties.dataUpdatedTime", isA(String.class)))
        ;
    }

    @Test
    public void forecastSections() throws Exception {
        logDebugResponse(
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.type", is("FeatureCollection")))
            .andExpect(jsonPath("$.features", hasSize(11)))
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

    @Test
    public void forecastSectionsByRoadNumber() throws Exception {
        logDebugResponse(
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + "?roadNumber=429")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.type", is("FeatureCollection")))
            .andExpect(jsonPath("$.features", hasSize(1)))
            .andExpect(jsonPath("$.features[0].properties.roadNumber", isA(Integer.class)))
        ;
    }
    @Test
    public void forecastSectionById() throws Exception {
        final String id = "00004_229_00307_1_0";
        logDebugResponse(
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + "/" + id)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.geometry.type", is("MultiLineString")))
            .andExpect(jsonPath("$.geometry.coordinates", hasSize(greaterThan(1))))
            .andExpect(jsonPath("$.type", is("Feature")))
            .andExpect(jsonPath("$.id", is(id)))

            .andExpect(jsonPath("$.properties.id", is(id)))
            .andExpect(jsonPath("$.properties.description", isA(String.class)))
            .andExpect(jsonPath("$.properties.roadSectionNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.properties.roadNumber", isA(Integer.class)))
            .andExpect(jsonPath("$.properties.length", isA(Integer.class)))
            .andExpect(jsonPath("$.properties.linkIds", hasSize(greaterThan(1))))
            .andExpect(jsonPath("$.properties.dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.properties.roadSegments", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.properties.roadSegments[0].startDistance", isA(Integer.class)))
            .andExpect(jsonPath("$.properties.roadSegments[0].endDistance", isA(Integer.class)))
        ;
    }

    @Test
    public void forecastSectionsForecastsSimple() throws Exception {
        logDebugResponse(
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + WeatherControllerV1.FORECASTS)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections", hasSize(4)))
            .andExpect(jsonPath("$.forecastSections[0].id", is("00001_001_000_0")))
            .andExpect(jsonPath("$.forecastSections[1].id", is("00001_006_000_0")))
            .andExpect(jsonPath("$.forecastSections[2].id", is("00001_009_000_0")))
            .andExpect(jsonPath("$.forecastSections[3].id", is("00002_001_000_0")))
            .andExpect(jsonPath("$.forecastSections[0].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts", hasSize(5)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].time", is(ForecastSectionTestHelper.TIMES[0])))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[1].time", is(ForecastSectionTestHelper.TIMES[1])))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[2].time", is(ForecastSectionTestHelper.TIMES[2])))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[3].time", is(ForecastSectionTestHelper.TIMES[3])))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[4].time", is(ForecastSectionTestHelper.TIMES[4])))

            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[1].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[2].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[3].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[4].dataUpdatedTime", isA(String.class)))
        ;
    }

    @Test
    public void forecastSectionsForecastsSimpleByRoadNumber() throws Exception {
        logDebugResponse(
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + WeatherControllerV1.FORECASTS + "?roadNumber=2")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections", hasSize(1)))
            .andExpect(jsonPath("$.forecastSections[0].id", is("00002_001_000_0")))
        ;
    }

    @Test
    public void forecastSectionsForecastsSimpleById() throws Exception {
        final String id = "00001_001_000_0";
        logDebugResponse(
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + "/" + id + WeatherControllerV1.FORECASTS)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.dataUpdatedTime", isA(String.class)))

            .andExpect(jsonPath("$.id", is(id)))
            .andExpect(jsonPath("$.forecasts", hasSize(5)))
            .andExpect(jsonPath("$.forecasts[0].time", is(ForecastSectionTestHelper.TIMES[0])))
            .andExpect(jsonPath("$.forecasts[1].time", is(ForecastSectionTestHelper.TIMES[1])))
            .andExpect(jsonPath("$.forecasts[2].time", is(ForecastSectionTestHelper.TIMES[2])))
            .andExpect(jsonPath("$.forecasts[3].time", is(ForecastSectionTestHelper.TIMES[3])))
            .andExpect(jsonPath("$.forecasts[4].time", is(ForecastSectionTestHelper.TIMES[4])))

            .andExpect(jsonPath("$.forecasts[0].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecasts[1].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecasts[2].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecasts[3].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecasts[4].dataUpdatedTime", isA(String.class)))
        ;
    }

    @Test
    public void forecastSectionsForecasts() throws Exception {
        logDebugResponse(
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + WeatherControllerV1.FORECASTS)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections", hasSize(4)))
            .andExpect(jsonPath("$.forecastSections[0].id", is("00004_229_00307_1_0")))
            .andExpect(jsonPath("$.forecastSections[1].id", is("00409_001_01796_0_0")))
            .andExpect(jsonPath("$.forecastSections[2].id", is("00429_003_00000_0_0")))
            .andExpect(jsonPath("$.forecastSections[3].id", is("00945_014_00000_0_0")))

            .andExpect(jsonPath("$.forecastSections[0].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts", hasSize(5)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].time", is(ForecastSectionTestHelper.TIMES[0])))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[1].time", is(ForecastSectionTestHelper.TIMES[1])))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[2].time", is(ForecastSectionTestHelper.TIMES[2])))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[3].time", is(ForecastSectionTestHelper.TIMES[3])))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[4].time", is(ForecastSectionTestHelper.TIMES[4])))

            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[1].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[2].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[3].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[4].dataUpdatedTime", isA(String.class)))
        ;
    }

    @Test
    public void forecastSectionsForecastsByRoadNumber() throws Exception {
        logDebugResponse(
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + WeatherControllerV1.FORECASTS + "?roadNumber=4")))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecastSections", hasSize(1)))
            .andExpect(jsonPath("$.forecastSections[0].id", is("00004_229_00307_1_0")))
        ;
    }
    @Test
    public void forecastSectionsForecastsById() throws Exception {
        final String id = "00004_229_00307_1_0";
        logDebugResponse(
            mockMvc.perform(get(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + "/" + id + WeatherControllerV1.FORECASTS)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.id", is(id)))
            .andExpect(jsonPath("$.forecasts", hasSize(5)))
            .andExpect(jsonPath("$.forecasts[0].time", is(ForecastSectionTestHelper.TIMES[0])))
            .andExpect(jsonPath("$.forecasts[1].time", is(ForecastSectionTestHelper.TIMES[1])))
            .andExpect(jsonPath("$.forecasts[2].time", is(ForecastSectionTestHelper.TIMES[2])))
            .andExpect(jsonPath("$.forecasts[3].time", is(ForecastSectionTestHelper.TIMES[3])))
            .andExpect(jsonPath("$.forecasts[4].time", is(ForecastSectionTestHelper.TIMES[4])))

            .andExpect(jsonPath("$.forecasts[0].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecasts[1].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecasts[2].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecasts[3].dataUpdatedTime", isA(String.class)))
            .andExpect(jsonPath("$.forecasts[4].dataUpdatedTime", isA(String.class)))
        ;
    }
}
