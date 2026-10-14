package fi.livi.digitraffic.tie.controller.weather;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
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

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.weather.forecast.ForecastSectionRepository;
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
            new ForecastSectionDataUpdateServiceV2(forecastSectionRepository, forecastSectionWeatherV2Repository, dataStatusService);
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
        logDebugResponse(mockMvc.perform(get(PATH + WeatherControllerV2.FORECASTS)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.dataUpdatedTime", is("2026-10-07T11:05:48Z")))
            .andExpect(jsonPath("$.forecastSections", hasSize(1)))
            .andExpect(jsonPath("$.forecastSections[0].id", is(ID)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts", hasSize(2)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].forecastName", is("0h")))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].type", is("OBSERVATION")))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].time", is("2026-10-07T12:00:00Z")))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].daylight", is(true)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].roadTemperature", is(5.5)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].windDirection", is(90)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].overallRoadCondition", is("POOR_CONDITION")))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].roadCondition", is("NOT_KNOWN")))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].weatherSymbol", is(109)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].reliability").doesNotExist())
            .andExpect(jsonPath("$.forecastSections[0].forecasts[1].roadCondition", is("ICE")))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[1].roadTemperature").doesNotExist())
            .andExpect(jsonPath("$.forecastSections[0].forecasts[1].weatherSymbol").doesNotExist());
    }

    @Test
    public void forecastsLastUpdated() throws Exception {
        mockMvc.perform(get(PATH + WeatherControllerV2.FORECASTS + "?lastUpdated=true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dataUpdatedTime", is("2026-10-07T11:05:48Z")))
            .andExpect(jsonPath("$.forecastSections", nullValue()));
    }

    @Test
    public void forecastsByRoadNumber() throws Exception {
        mockMvc.perform(get(PATH + WeatherControllerV2.FORECASTS + "?roadNumber=4"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.forecastSections", hasSize(1)));
        mockMvc.perform(get(PATH + WeatherControllerV2.FORECASTS + "?roadNumber=1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.forecastSections", hasSize(0)));
    }

    @Test
    public void forecastsById() throws Exception {
        mockMvc.perform(get(PATH + "/" + ID + WeatherControllerV2.FORECASTS))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(ID)))
            .andExpect(jsonPath("$.forecasts", hasSize(2)));
    }

    @Test
    public void forecastsByUnknownIdIsNotFound() throws Exception {
        mockMvc.perform(get(PATH + "/UNKNOWN" + WeatherControllerV2.FORECASTS)).andExpect(status().isNotFound());
    }

    @Test
    public void forecastSectionsMetadata() throws Exception {
        mockMvc.perform(get(PATH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("FeatureCollection")))
            .andExpect(jsonPath("$.features", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.features[?(@.id == '" + ID + "')]", hasSize(1)));
        mockMvc.perform(get(PATH + "?lastUpdated=true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.features", hasSize(0)));
    }

    @Test
    public void forecastSectionMetadataById() throws Exception {
        mockMvc.perform(get(PATH + "/" + ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("Feature")))
            .andExpect(jsonPath("$.id", is(ID)));
        mockMvc.perform(get(PATH + "/UNKNOWN")).andExpect(status().isNotFound());
    }

    @Test
    public void simpleForecastsOfAllSections() throws Exception {
        logDebugResponse(mockMvc.perform(get(SIMPLE_PATH + WeatherControllerV2.FORECASTS)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.dataUpdatedTime", is("2026-10-07T11:00:00Z")))
            .andExpect(jsonPath("$.forecastSections", hasSize(1)))
            .andExpect(jsonPath("$.forecastSections[0].id", is(SIMPLE_ID)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts", hasSize(1)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].roadTemperature", is(1.5)))
            .andExpect(jsonPath("$.forecastSections[0].forecasts[0].overallRoadCondition", is("EXTREMELY_POOR_CONDITION")));
        mockMvc.perform(get(SIMPLE_PATH + WeatherControllerV2.FORECASTS + "?lastUpdated=true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dataUpdatedTime", is("2026-10-07T11:00:00Z")))
            .andExpect(jsonPath("$.forecastSections", nullValue()));
        mockMvc.perform(get(SIMPLE_PATH + WeatherControllerV2.FORECASTS + "?roadNumber=4"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.forecastSections", hasSize(0)));
    }

    @Test
    public void simpleForecastsById() throws Exception {
        mockMvc.perform(get(SIMPLE_PATH + "/" + SIMPLE_ID + WeatherControllerV2.FORECASTS))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(SIMPLE_ID)))
            .andExpect(jsonPath("$.forecasts", hasSize(1)));
        mockMvc.perform(get(SIMPLE_PATH + "/" + ID + WeatherControllerV2.FORECASTS)).andExpect(status().isNotFound());
        mockMvc.perform(get(PATH + "/" + SIMPLE_ID + WeatherControllerV2.FORECASTS)).andExpect(status().isNotFound());
    }

    @Test
    public void simpleForecastSectionsMetadata() throws Exception {
        logDebugResponse(mockMvc.perform(get(SIMPLE_PATH)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(DT_JSON_CONTENT_TYPE))
            .andExpect(jsonPath("$.type", is("FeatureCollection")))
            .andExpect(jsonPath("$.features", hasSize(10)))
            .andExpect(jsonPath("$.features[0].geometry.type", is("LineString")))
            .andExpect(jsonPath("$.features[?(@.id == '" + SIMPLE_ID + "')]", hasSize(1)))
            .andExpect(jsonPath("$.features[?(@.id == '" + ID + "')]", hasSize(0)));
        mockMvc.perform(get(SIMPLE_PATH + "?roadNumber=1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.features", hasSize(7)));
        mockMvc.perform(get(SIMPLE_PATH + "?lastUpdated=true"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.features", hasSize(0)));
    }

    @Test
    public void simpleForecastSectionMetadataById() throws Exception {
        mockMvc.perform(get(SIMPLE_PATH + "/" + SIMPLE_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("Feature")))
            .andExpect(jsonPath("$.id", is(SIMPLE_ID)))
            .andExpect(jsonPath("$.geometry.type", is("LineString")));
        mockMvc.perform(get(SIMPLE_PATH + "/" + ID)).andExpect(status().isNotFound());
    }
}
