package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.WEATHER_HISTORY_DATA_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasSize;

import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fi.livi.digitraffic.tie.AbstractRestWebTest;

public class WeatherDataHistoryControllerTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(WeatherDataHistoryControllerTest.class);

    private ResultActions getJson(final String url) throws Exception {
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(API_V2_BASE_PATH + API_DATA_PART_PATH + WEATHER_HISTORY_DATA_PATH + url);

        get.contentType(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(get);
        log.info("JSON:\n{}", result.andReturn().getResponse().getContentAsString());
        return result;
    }

    @Before
    public void setUp() {
        ZonedDateTime refTime = ZonedDateTime.now();

        entityManager.createNativeQuery("insert into sensor_value_history(id, road_station_id, road_station_sensor_id, value, measured) values \n" +
            "(nextval('seq_sensor_value_history'), 20000, 100, 5, timestamp with time zone '" + refTime.minusHours(1).toInstant() + "');").executeUpdate();

        entityManager.createNativeQuery("insert into sensor_value_history(id, road_station_id, road_station_sensor_id, value, measured) values \n" +
            "(nextval('seq_sensor_value_history'), 20000, 101, 4, timestamp with time zone '" + refTime.minusMinutes(55).toInstant() + "');").executeUpdate();

        entityManager.createNativeQuery("insert into sensor_value_history(id, road_station_id, road_station_sensor_id, value, measured) values \n" +
            "(nextval('seq_sensor_value_history'), 20000, 101, 3, timestamp with time zone '" + refTime.minusMinutes(50).toInstant() + "');").executeUpdate();

        entityManager.createNativeQuery("insert into sensor_value_history(id, road_station_id, road_station_sensor_id, value, measured) values \n" +
            "(nextval('seq_sensor_value_history'), 20000, 100, 6, timestamp with time zone '" + refTime.minusMinutes(40).toInstant() + "');").executeUpdate();

        entityManager.createNativeQuery("insert into sensor_value_history(id, road_station_id, road_station_sensor_id, value, measured) values \n" +
            "(nextval('seq_sensor_value_history'), 20001, 101, 2, timestamp with time zone '" + refTime.minusHours(1).toInstant() + "');").executeUpdate();

        entityManager.createNativeQuery("insert into sensor_value_history(id, road_station_id, road_station_sensor_id, value, measured) values \n" +
            "(nextval('seq_sensor_value_history'), 20001, 100, 1, timestamp with time zone '" + refTime.minusMinutes(45).toInstant() + "');").executeUpdate();
    }

    @Test
    public void onlyRequestedStationId() throws Exception {
        getJson("/20000?from=" + ZonedDateTime.now().minusHours(2).toInstant())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", not(hasSize(0))))
            .andExpect(jsonPath("$.[?(@.roadStationId != 20000)]", hasSize(0)));
    }

    @Test
    public void onlyRequestedSensorId() throws Exception {
        getJson("/20000/100?from=" + ZonedDateTime.now().minusHours(2).toInstant())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$.[?(@.sensorId != 100)]", hasSize(0)));
    }

    @Test
    public void insideTimeWindow() throws Exception {
        getJson("/20000?from=" + ZonedDateTime.now().minusMinutes(58).toInstant() + "&to=" + ZonedDateTime.now().minusMinutes(45).toInstant())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void nonExistingStationId() throws Exception {
        getJson("/12345?from=1970-01-01T00:00:00Z")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void invalidRequest() throws Exception {
        getJson("/20000?from=not_time")
            .andExpect(status().isBadRequest());
    }
}
