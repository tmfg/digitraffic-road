package fi.livi.digitraffic.tie.controller.weather;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.io.IOException;

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
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionClient;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionDataUpdateService;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionDataUpdater;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionMetadataUpdateService;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionTestHelper;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionV1MetadataUpdater;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionV2MetadataUpdater;
import okhttp3.mockwebserver.MockWebServer;

public class WeatherControllerV1ForecastTest extends AbstractRestWebTest {

    private static final Logger log = LoggerFactory.getLogger(WeatherControllerV1ForecastTest.class);

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;

    @Autowired
    private DataStatusService dataStatusService;

    @Autowired
    private ForecastSectionTestHelper forecastSectionTestHelper;

    @Autowired
    private V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao;

    private MockWebServer server;

    @BeforeEach
    public void initData() throws IOException {
        final StopWatch start = StopWatch.createStarted();

        server = new MockWebServer();

        final ForecastSectionClient forecastSectionClient = forecastSectionTestHelper.createForecastSectionClient(server);
        final ForecastSectionDataUpdateService forecastSectionDataUpdateService =
                new ForecastSectionDataUpdateService(forecastSectionRepository, dataStatusService);

        final ForecastSectionMetadataUpdateService forecastSectionMetadataUpdateService =
                new ForecastSectionMetadataUpdateService(forecastSectionRepository, v2ForecastSectionMetadataDao, dataStatusService);

        final ForecastSectionV1MetadataUpdater forecastSectionMetadataUpdater =
            new ForecastSectionV1MetadataUpdater(forecastSectionClient, forecastSectionMetadataUpdateService);

        final ForecastSectionV2MetadataUpdater forecastSectionV2MetadataUpdater =
            new ForecastSectionV2MetadataUpdater(forecastSectionClient, forecastSectionMetadataUpdateService);

        final ForecastSectionDataUpdater forecastSectionDataUpdater =
            new ForecastSectionDataUpdater(forecastSectionClient, forecastSectionDataUpdateService);

        forecastSectionTestHelper.serveGzippedMetadata(server,1);
        forecastSectionTestHelper.serveGzippedMetadata(server,2);

        forecastSectionTestHelper.serveGzippedData(server,1);
        forecastSectionTestHelper.serveGzippedData(server,2);

        forecastSectionMetadataUpdater.updateForecastSectionV1Metadata();
        forecastSectionV2MetadataUpdater.updateForecastSectionsV2Metadata();
        forecastSectionDataUpdater.updateForecastSectionWeatherDataV1();
        forecastSectionDataUpdater.updateForecastSectionWeatherDataV2();
        entityManager.flush();
        entityManager.clear();
        log.info("Init data tookMs={}", start.getDuration().toMillis());
    }

    @AfterEach
    public void after() throws IOException {
        server.close();
    }

    @Test
    public void forecastSectionsSimple() throws Exception {
        logDebugResponse(executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.type").value(is("FeatureCollection"))
            .jsonPath("$.features").value(hasSize(10))
            .jsonPath("$.features[0].geometry.type").value(is("LineString"))
            .jsonPath("$.features[0].geometry.coordinates").value(hasSize(greaterThan(1)))
            .jsonPath("$.features[0].type").value(is("Feature"))
            .jsonPath("$.features[0].id").value(isA(String.class))
            .jsonPath("$.features[0].properties.id").value(isA(String.class))
            .jsonPath("$.features[0].properties.description").value(isA(String.class))
            .jsonPath("$.features[0].properties.roadSectionNumber").value(isA(Integer.class))
            .jsonPath("$.features[0].properties.roadNumber").value(isA(Integer.class))
            .jsonPath("$.features[0].properties.roadSectionVersionNumber").value(isA(Integer.class))
            .jsonPath("$.features[0].properties.dataUpdatedTime").value(isA(String.class))
        ;
    }

    @Test
    public void forecastSectionsSimpleByRoadNumber() throws Exception {
        logDebugResponse(executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + "?roadNumber=1")
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.type").value(is("FeatureCollection"))
            .jsonPath("$.features").value(hasSize(7))
            .jsonPath("$.features[0].properties.roadNumber").value(is(1))
        ;
    }

    @Test
    public void forecastSectionsSimpleById() throws Exception {
        final String id = "00001_001_000_0";
        logDebugResponse(
            executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + "/" + id)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.geometry.type").value(is("LineString"))
            .jsonPath("$.geometry.coordinates").value(hasSize(greaterThan(1)))
            .jsonPath("$.type").value(is("Feature"))
            .jsonPath("$.id").value(is(id))
            .jsonPath("$.properties.id").value(is(id))
            .jsonPath("$.properties.description").value(isA(String.class))
            .jsonPath("$.properties.roadSectionNumber").value(isA(Integer.class))
            .jsonPath("$.properties.roadNumber").value(isA(Integer.class))
            .jsonPath("$.properties.roadSectionVersionNumber").value(isA(Integer.class))
            .jsonPath("$.properties.dataUpdatedTime").value(isA(String.class))
        ;
    }

    @Test
    public void forecastSections() throws Exception {
        logDebugResponse(
            executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.type").value(is("FeatureCollection"))
            .jsonPath("$.features").value(hasSize(11))
            .jsonPath("$.features[0].geometry.type").value(is("MultiLineString"))
            .jsonPath("$.features[0].geometry.coordinates").value(hasSize(greaterThan(1)))
            .jsonPath("$.features[0].type").value(is("Feature"))
            .jsonPath("$.features[0].id").value(isA(String.class))

            .jsonPath("$.features[0].properties.id").value(isA(String.class))
            .jsonPath("$.features[0].properties.description").value(isA(String.class))
            .jsonPath("$.features[0].properties.roadSectionNumber").value(isA(Integer.class))
            .jsonPath("$.features[0].properties.roadNumber").value(isA(Integer.class))
            .jsonPath("$.features[0].properties.length").value(isA(Integer.class))
            .jsonPath("$.features[0].properties.linkIds").value(hasSize(greaterThan(1)))
            .jsonPath("$.features[0].properties.dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.features[0].properties.roadSegments").value(hasSize(greaterThanOrEqualTo(1)))
            .jsonPath("$.features[0].properties.roadSegments[0].startDistance").value(isA(Integer.class))
            .jsonPath("$.features[0].properties.roadSegments[0].endDistance").value(isA(Integer.class))
        ;
    }

    @Test
    public void forecastSectionsByRoadNumber() throws Exception {
        logDebugResponse(
            executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + "?roadNumber=429")
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.type").value(is("FeatureCollection"))
            .jsonPath("$.features").value(hasSize(1))
            .jsonPath("$.features[0].properties.roadNumber").value(isA(Integer.class))
        ;
    }
    @Test
    public void forecastSectionById() throws Exception {
        final String id = "00004_229_00307_1_0";
        logDebugResponse(
            executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + "/" + id)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.geometry.type").value(is("MultiLineString"))
            .jsonPath("$.geometry.coordinates").value(hasSize(greaterThan(1)))
            .jsonPath("$.type").value(is("Feature"))
            .jsonPath("$.id").value(is(id))

            .jsonPath("$.properties.id").value(is(id))
            .jsonPath("$.properties.description").value(isA(String.class))
            .jsonPath("$.properties.roadSectionNumber").value(isA(Integer.class))
            .jsonPath("$.properties.roadNumber").value(isA(Integer.class))
            .jsonPath("$.properties.length").value(isA(Integer.class))
            .jsonPath("$.properties.linkIds").value(hasSize(greaterThan(1)))
            .jsonPath("$.properties.dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.properties.roadSegments").value(hasSize(greaterThanOrEqualTo(1)))
            .jsonPath("$.properties.roadSegments[0].startDistance").value(isA(Integer.class))
            .jsonPath("$.properties.roadSegments[0].endDistance").value(isA(Integer.class))
        ;
    }

    @Test
    public void forecastSectionsForecastsSimple() throws Exception {
        logDebugResponse(
            executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + WeatherControllerV1.FORECASTS)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections").value(hasSize(4))
            .jsonPath("$.forecastSections[0].id").value(is("00001_001_000_0"))
            .jsonPath("$.forecastSections[1].id").value(is("00001_006_000_0"))
            .jsonPath("$.forecastSections[2].id").value(is("00001_009_000_0"))
            .jsonPath("$.forecastSections[3].id").value(is("00002_001_000_0"))
            .jsonPath("$.forecastSections[0].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections[0].forecasts").value(hasSize(5))
            .jsonPath("$.forecastSections[0].forecasts[0].time").value(is(ForecastSectionTestHelper.TIMES[0]))
            .jsonPath("$.forecastSections[0].forecasts[1].time").value(is(ForecastSectionTestHelper.TIMES[1]))
            .jsonPath("$.forecastSections[0].forecasts[2].time").value(is(ForecastSectionTestHelper.TIMES[2]))
            .jsonPath("$.forecastSections[0].forecasts[3].time").value(is(ForecastSectionTestHelper.TIMES[3]))
            .jsonPath("$.forecastSections[0].forecasts[4].time").value(is(ForecastSectionTestHelper.TIMES[4]))

            .jsonPath("$.forecastSections[0].forecasts[0].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections[0].forecasts[1].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections[0].forecasts[2].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections[0].forecasts[3].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections[0].forecasts[4].dataUpdatedTime").value(isA(String.class))
        ;
    }

    @Test
    public void forecastSectionsForecastsSimpleByRoadNumber() throws Exception {
        logDebugResponse(
            executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + WeatherControllerV1.FORECASTS + "?roadNumber=2")
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections").value(hasSize(1))
            .jsonPath("$.forecastSections[0].id").value(is("00002_001_000_0"))
        ;
    }

    @Test
    public void forecastSectionsForecastsSimpleById() throws Exception {
        final String id = "00001_001_000_0";
        logDebugResponse(
            executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS_SIMPLE + "/" + id + WeatherControllerV1.FORECASTS)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.dataUpdatedTime").value(isA(String.class))

            .jsonPath("$.id").value(is(id))
            .jsonPath("$.forecasts").value(hasSize(5))
            .jsonPath("$.forecasts[0].time").value(is(ForecastSectionTestHelper.TIMES[0]))
            .jsonPath("$.forecasts[1].time").value(is(ForecastSectionTestHelper.TIMES[1]))
            .jsonPath("$.forecasts[2].time").value(is(ForecastSectionTestHelper.TIMES[2]))
            .jsonPath("$.forecasts[3].time").value(is(ForecastSectionTestHelper.TIMES[3]))
            .jsonPath("$.forecasts[4].time").value(is(ForecastSectionTestHelper.TIMES[4]))

            .jsonPath("$.forecasts[0].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecasts[1].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecasts[2].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecasts[3].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecasts[4].dataUpdatedTime").value(isA(String.class))
        ;
    }

    @Test
    public void forecastSectionsForecasts() throws Exception {
        logDebugResponse(
            executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + WeatherControllerV1.FORECASTS)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections").value(hasSize(4))
            .jsonPath("$.forecastSections[0].id").value(is("00004_229_00307_1_0"))
            .jsonPath("$.forecastSections[1].id").value(is("00409_001_01796_0_0"))
            .jsonPath("$.forecastSections[2].id").value(is("00429_003_00000_0_0"))
            .jsonPath("$.forecastSections[3].id").value(is("00945_014_00000_0_0"))

            .jsonPath("$.forecastSections[0].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections[0].forecasts").value(hasSize(5))
            .jsonPath("$.forecastSections[0].forecasts[0].time").value(is(ForecastSectionTestHelper.TIMES[0]))
            .jsonPath("$.forecastSections[0].forecasts[1].time").value(is(ForecastSectionTestHelper.TIMES[1]))
            .jsonPath("$.forecastSections[0].forecasts[2].time").value(is(ForecastSectionTestHelper.TIMES[2]))
            .jsonPath("$.forecastSections[0].forecasts[3].time").value(is(ForecastSectionTestHelper.TIMES[3]))
            .jsonPath("$.forecastSections[0].forecasts[4].time").value(is(ForecastSectionTestHelper.TIMES[4]))

            .jsonPath("$.forecastSections[0].forecasts[0].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections[0].forecasts[1].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections[0].forecasts[2].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections[0].forecasts[3].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections[0].forecasts[4].dataUpdatedTime").value(isA(String.class))
        ;
    }

    @Test
    public void forecastSectionsForecastsByRoadNumber() throws Exception {
        logDebugResponse(
            executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + WeatherControllerV1.FORECASTS + "?roadNumber=4")
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecastSections").value(hasSize(1))
            .jsonPath("$.forecastSections[0].id").value(is("00004_229_00307_1_0"))
        ;
    }
    @Test
    public void forecastSectionsForecastsById() throws Exception {
        final String id = "00004_229_00307_1_0";
        logDebugResponse(
            executeGet(WeatherControllerV1.API_WEATHER_V1 + WeatherControllerV1.FORECAST_SECTIONS + "/" + id + WeatherControllerV1.FORECASTS)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.id").value(is(id))
            .jsonPath("$.forecasts").value(hasSize(5))
            .jsonPath("$.forecasts[0].time").value(is(ForecastSectionTestHelper.TIMES[0]))
            .jsonPath("$.forecasts[1].time").value(is(ForecastSectionTestHelper.TIMES[1]))
            .jsonPath("$.forecasts[2].time").value(is(ForecastSectionTestHelper.TIMES[2]))
            .jsonPath("$.forecasts[3].time").value(is(ForecastSectionTestHelper.TIMES[3]))
            .jsonPath("$.forecasts[4].time").value(is(ForecastSectionTestHelper.TIMES[4]))

            .jsonPath("$.forecasts[0].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecasts[1].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecasts[2].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecasts[3].dataUpdatedTime").value(isA(String.class))
            .jsonPath("$.forecasts[4].dataUpdatedTime").value(isA(String.class))
        ;
    }
}
