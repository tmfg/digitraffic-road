package fi.livi.digitraffic.tie.controller.weather;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionWeatherV2Dao;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionWeatherV2Repository;
import fi.livi.digitraffic.tie.dao.weather.forecast.V2ForecastSectionMetadataDao;
import fi.livi.digitraffic.tie.dto.weather.forecast.client.ForecastSectionDataDtoV2;
import fi.livi.digitraffic.tie.service.DataStatusService;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastDataSetV2;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionClient;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionDataUpdateServiceV2;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionMetadataUpdateService;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionTestHelper;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionV1MetadataUpdater;
import fi.livi.digitraffic.tie.service.weather.forecast.ForecastSectionV2MetadataUpdater;
import okhttp3.mockwebserver.MockWebServer;
import tools.jackson.databind.ObjectMapper;

public class WeatherControllerV2ForecastTest extends AbstractRestWebTest {
    private static final String ID = "00004_229_00307_1_0";
    private static final String PATH = WeatherControllerV2.API_WEATHER_V2 + WeatherControllerV2.FORECAST_SECTIONS;
    private static final String SIMPLE_ID = "00001_001_000_0";
    private static final String SIMPLE_PATH = WeatherControllerV2.API_WEATHER_V2 + WeatherControllerV2.FORECAST_SECTIONS_SIMPLE;

    @Autowired
    private ForecastSectionRepository forecastSectionRepository;
    @Autowired
    private ForecastSectionWeatherV2Repository forecastSectionWeatherV2Repository;
    @Autowired
    private ForecastSectionWeatherV2Dao forecastSectionWeatherV2Dao;
    @Autowired
    private DataStatusService dataStatusService;
    @Autowired
    private ForecastSectionTestHelper forecastSectionTestHelper;
    @Autowired
    private V2ForecastSectionMetadataDao v2ForecastSectionMetadataDao;
    @Autowired
    private ObjectMapper objectMapper;

    private MockWebServer server;

    @BeforeEach
    public void initData() throws IOException {
        server = new MockWebServer();
        forecastSectionTestHelper.serveGzippedMetadata(server, 1);
        forecastSectionTestHelper.serveGzippedMetadata(server, 2);
        final ForecastSectionClient client = forecastSectionTestHelper.createForecastSectionClient(server);
        final ForecastSectionMetadataUpdateService metadataUpdateService =
            new ForecastSectionMetadataUpdateService(forecastSectionRepository, v2ForecastSectionMetadataDao, dataStatusService);
        new ForecastSectionV1MetadataUpdater(client, metadataUpdateService).updateForecastSectionV1Metadata();
        new ForecastSectionV2MetadataUpdater(client, metadataUpdateService).updateForecastSectionsV2Metadata();

        final String json = """
            {"dataUpdatedTime":"2026-10-07T11:05:48Z","forecastSections":[{"id":"%s","forecasts":[
              {"time":"2026-10-07T12:00:00Z","type":"OBSERVATION","forecastName":"0h","daylight":true,"roadTemperature":5.5,"temperature":4.0,
               "windSpeed":2.0,"windDirection":90,"overallRoadCondition":2,"roadCondition":0,"weatherSymbol":109,"reliability":null},
              {"time":"2026-10-07T14:00:00Z","type":"FORECAST","forecastName":"2h","daylight":false,"roadTemperature":null,"temperature":3.5,
               "windSpeed":null,"windDirection":null,"overallRoadCondition":1,"roadCondition":7,"weatherSymbol":null,"reliability":null}
            ]}]}""".formatted(ID);
        final ForecastSectionDataUpdateServiceV2 dataUpdateService =
            new ForecastSectionDataUpdateServiceV2(forecastSectionRepository, forecastSectionWeatherV2Repository, forecastSectionWeatherV2Dao, dataStatusService);
        dataUpdateService.updateForecastSectionWeatherData(ForecastDataSetV2.DETAILED, objectMapper.readValue(json, ForecastSectionDataDtoV2.class));

        final String simpleJson = """
            {"dataUpdatedTime":"2026-10-07T11:00:00Z","forecastSections":[{"id":"%s","forecasts":[
              {"time":"2026-10-07T12:00:00Z","type":"OBSERVATION","forecastName":"0h","daylight":true,"roadTemperature":1.5,"temperature":0.5,
               "windSpeed":3.0,"windDirection":180,"overallRoadCondition":3,"roadCondition":5,"weatherSymbol":1,"reliability":null}
            ]}]}""".formatted(SIMPLE_ID);
        dataUpdateService.updateForecastSectionWeatherData(ForecastDataSetV2.SIMPLE, objectMapper.readValue(simpleJson, ForecastSectionDataDtoV2.class));
        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    public void after() throws IOException {
        server.close();
    }

    @Test
    public void forecastsOfAllSections() throws Exception {
        logDebugResponse(executeGet(PATH + WeatherControllerV2.FORECASTS)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.dataUpdatedTime").value(is("2026-10-07T11:05:48Z"))
            .jsonPath("$.forecastSections").value(hasSize(1))
            .jsonPath("$.forecastSections[0].id").value(is(ID))
            .jsonPath("$.forecastSections[0].forecasts").value(hasSize(2))
            .jsonPath("$.forecastSections[0].forecasts[0].forecastName").value(is("0h"))
            .jsonPath("$.forecastSections[0].forecasts[0].type").value(is("OBSERVATION"))
            .jsonPath("$.forecastSections[0].forecasts[0].time").value(is("2026-10-07T12:00:00Z"))
            .jsonPath("$.forecastSections[0].forecasts[0].daylight").value(is(true))
            .jsonPath("$.forecastSections[0].forecasts[0].roadTemperature").value(is(5.5))
            .jsonPath("$.forecastSections[0].forecasts[0].windDirection").value(is(90))
            .jsonPath("$.forecastSections[0].forecasts[0].overallRoadCondition").value(is("POOR_CONDITION"))
            .jsonPath("$.forecastSections[0].forecasts[0].roadCondition").value(is("NOT_KNOWN"))
            .jsonPath("$.forecastSections[0].forecasts[0].weatherSymbol").value(is(109))
            .jsonPath("$.forecastSections[0].forecasts[0].reliability").doesNotExist()
            .jsonPath("$.forecastSections[0].forecasts[1].roadCondition").value(is("ICE"))
            .jsonPath("$.forecastSections[0].forecasts[1].roadTemperature").doesNotExist()
            .jsonPath("$.forecastSections[0].forecasts[1].weatherSymbol").doesNotExist();
    }

    @Test
    public void forecastsLastUpdated() throws Exception {
        executeGet(PATH + WeatherControllerV2.FORECASTS + "?lastUpdated=true")
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.dataUpdatedTime").value(is("2026-10-07T11:05:48Z"))
            .jsonPath("$.forecastSections").doesNotExist();
    }

    @Test
    public void forecastsByRoadNumber() throws Exception {
        executeGet(PATH + WeatherControllerV2.FORECASTS + "?roadNumber=4")
            .expectStatus().isOk()
            .expectBody().jsonPath("$.forecastSections").value(hasSize(1));
        executeGet(PATH + WeatherControllerV2.FORECASTS + "?roadNumber=1")
            .expectStatus().isOk()
            .expectBody().jsonPath("$.forecastSections").value(hasSize(0));
    }

    @Test
    public void forecastsById() throws Exception {
        executeGet(PATH + "/" + ID + WeatherControllerV2.FORECASTS)
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").value(is(ID))
            .jsonPath("$.forecasts").value(hasSize(2));
    }

    @Test
    public void forecastsByUnknownIdIsNotFound() throws Exception {
        executeGet(PATH + "/UNKNOWN" + WeatherControllerV2.FORECASTS).expectStatus().isNotFound();
    }

    @Test
    public void forecastSectionsMetadata() throws Exception {
        executeGet(PATH)
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.type").value(is("FeatureCollection"))
            .jsonPath("$.features").value(hasSize(greaterThan(0)))
            .jsonPath("$.features[?(@.id == '" + ID + "')]").value(hasSize(1));
        executeGet(PATH + "?lastUpdated=true")
            .expectStatus().isOk()
            .expectBody().jsonPath("$.features").value(hasSize(0));
    }

    @Test
    public void forecastSectionMetadataById() throws Exception {
        executeGet(PATH + "/" + ID)
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.type").value(is("Feature"))
            .jsonPath("$.id").value(is(ID));
        executeGet(PATH + "/UNKNOWN").expectStatus().isNotFound();
    }

    @Test
    public void simpleForecastsOfAllSections() throws Exception {
        logDebugResponse(executeGet(SIMPLE_PATH + WeatherControllerV2.FORECASTS)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.dataUpdatedTime").value(is("2026-10-07T11:00:00Z"))
            .jsonPath("$.forecastSections").value(hasSize(1))
            .jsonPath("$.forecastSections[0].id").value(is(SIMPLE_ID))
            .jsonPath("$.forecastSections[0].forecasts").value(hasSize(1))
            .jsonPath("$.forecastSections[0].forecasts[0].roadTemperature").value(is(1.5))
            .jsonPath("$.forecastSections[0].forecasts[0].overallRoadCondition").value(is("EXTREMELY_POOR_CONDITION"));
        executeGet(SIMPLE_PATH + WeatherControllerV2.FORECASTS + "?lastUpdated=true")
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.dataUpdatedTime").value(is("2026-10-07T11:00:00Z"))
            .jsonPath("$.forecastSections").doesNotExist();
        executeGet(SIMPLE_PATH + WeatherControllerV2.FORECASTS + "?roadNumber=4")
            .expectStatus().isOk()
            .expectBody().jsonPath("$.forecastSections").value(hasSize(0));
    }

    @Test
    public void simpleForecastsById() throws Exception {
        executeGet(SIMPLE_PATH + "/" + SIMPLE_ID + WeatherControllerV2.FORECASTS)
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").value(is(SIMPLE_ID))
            .jsonPath("$.forecasts").value(hasSize(1));
        executeGet(SIMPLE_PATH + "/" + ID + WeatherControllerV2.FORECASTS).expectStatus().isNotFound();
        executeGet(PATH + "/" + SIMPLE_ID + WeatherControllerV2.FORECASTS).expectStatus().isNotFound();
    }

    @Test
    public void simpleForecastSectionsMetadata() throws Exception {
        logDebugResponse(executeGet(SIMPLE_PATH)
            .expectStatus().isOk()
            .expectHeader().contentType(DT_JSON_CONTENT_TYPE))
            .jsonPath("$.type").value(is("FeatureCollection"))
            .jsonPath("$.features").value(hasSize(10))
            .jsonPath("$.features[0].geometry.type").value(is("LineString"))
            .jsonPath("$.features[?(@.id == '" + SIMPLE_ID + "')]").value(hasSize(1))
            .jsonPath("$.features[?(@.id == '" + ID + "')]").value(hasSize(0));
        executeGet(SIMPLE_PATH + "?roadNumber=1")
            .expectStatus().isOk()
            .expectBody().jsonPath("$.features").value(hasSize(7));
        executeGet(SIMPLE_PATH + "?lastUpdated=true")
            .expectStatus().isOk()
            .expectBody().jsonPath("$.features").value(hasSize(0));
    }

    @Test
    public void simpleForecastSectionMetadataById() throws Exception {
        executeGet(SIMPLE_PATH + "/" + SIMPLE_ID)
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.type").value(is("Feature"))
            .jsonPath("$.id").value(is(SIMPLE_ID))
            .jsonPath("$.geometry.type").value(is("LineString"));
        executeGet(SIMPLE_PATH + "/" + ID).expectStatus().isNotFound();
    }
}
