package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_HISTORY_DATA_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_BETA_BASE_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasSize;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.dao.SensorValueHistoryRepository;
import fi.livi.digitraffic.tie.helper.SensorValueHistoryBuilder;

public class WeatherDataHistoryControllerTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(WeatherDataHistoryControllerTest.class);

    @Autowired
    private SensorValueHistoryRepository repository;

    private ResultActions getJson(final String url) throws Exception {
        // Beta API
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(API_BETA_BASE_PATH + WEATHER_HISTORY_DATA_PATH + url);
        // Actual API
        //final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(API_V2_BASE_PATH + API_DATA_PART_PATH + WEATHER_HISTORY_DATA_PATH + url);

        get.contentType(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(get);
        log.info("JSON:\n{}", result.andReturn().getResponse().getContentAsString());
        return result;
    }

    @Test
    public void onlyRequestedStationId() throws Exception {
        new SensorValueHistoryBuilder(repository, log)
            .buildWithStationId(20,20000,5,1,120)
            .buildWithStationId(20, 100, 5, 1, 120)
            .save();

        getJson("/20000?from=" + getTime(120))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", not(hasSize(0))))
            .andExpect(jsonPath("$.[?(@.roadStationId != 20000)]", hasSize(0)));
    }

    @Test
    public void onlyRequestedSensorId() throws Exception {
        SensorValueHistoryBuilder builder = new SensorValueHistoryBuilder(repository, log)
            .buildWithStationIdAndSensorId(20, 20000, 100, 1, 120)
            .buildWithStationIdAndSensorId(20, 20000, 125, 1, 120)
            .save();

        getJson("/20000/100?from=" + getTime(120))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(builder.getElementCountAt(0))))
            .andExpect(jsonPath("$.[?(@.sensorId != 100)]", hasSize(0)));
    }

    @Test
    public void insideTimeWindow() throws Exception {
        SensorValueHistoryBuilder builder = new SensorValueHistoryBuilder(repository, log)
            .buildWithStationId(20, 20000, 10,40, 50)
            .buildWithStationId(20, 20000, 10, 52, 100)
            .buildWithStationId(20, 20000, 10,1, 38)
            .save();

        getJson("/20000?from=" + getTime(51) + "&to=" + getTime(39))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(builder.getElementCountAt(0))));
    }

    @Test
    public void nonExistingStationId() throws Exception {
        new SensorValueHistoryBuilder(repository, log)
            .buildRandom(20, 100, 100, 1, 480)
            .save();

        getJson("/12345?from=1970-01-01T00:00:00Z")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void nonExistingSensorId() throws Exception {
        new SensorValueHistoryBuilder(repository, log)
            .buildWithStationIdAndSensorId(20, 100, 50, 1, 480)
            .save();

        getJson("/100/10?from=" + getTime(480))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void invalidRequest() throws Exception {
        getJson("/20000?from=not_time")
            .andExpect(status().isBadRequest());
    }

    @Test
    public void stationIdWithNoTimes() throws Exception {
        SensorValueHistoryBuilder builder = new SensorValueHistoryBuilder(repository, log)
            .buildWithStationId(20, 20000, 10,0, 60)
            .buildWithStationId(10, 20000, 10, 61, 120)
            .save();

        getJson("/20000")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(builder.getElementCountAt(0))));
    }

    @Test
    public void stationAndSensorIdsWithNoTime() throws Exception {
        SensorValueHistoryBuilder builder = new SensorValueHistoryBuilder(repository, log)
            .buildWithStationIdAndSensorId(20, 20000, 10,0, 60)
            .buildWithStationIdAndSensorId(10, 20000, 10, 61, 120)
            .save();

        getJson("/20000/10")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(builder.getElementCountAt(0))));
    }

    @Test
    public void wrongTimes() throws Exception {
        getJson("123456/from=" + getTime(1) + "&to=" + getTime(5))
            .andExpect(status().is4xxClientError());
    }

    private Instant getTime(int minusMinutes) {
        return ZonedDateTime.now().minusMinutes(minusMinutes).toInstant();
    }
}
