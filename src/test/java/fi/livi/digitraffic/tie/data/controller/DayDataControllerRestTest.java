package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.RestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

/**
 * Test material contains journeytime_medians from 25.8.2015.
 * So we adjust the end_timestamp in database to now - 24 h for this test
 * to find history data for yesterday.
 */
public class DayDataControllerRestTest extends RestTest {
    private long days = 0;

    private final LocalDate DATE = LocalDate.of(2015, 8, 25);

    @Before
    public void alterEndTimeStamp() {
        days = ChronoUnit.DAYS.between(DATE, LocalDate.now()) - 1;

        jdbcTemplate.update("update journeytime_median set end_timestamp = end_timestamp + ?", days);
    }

    @After
    public void restoreEndTimestamp() {
        jdbcTemplate.update("update journeytime_median set end_timestamp = end_timestamp - ?", days);
    }

    @Test
    public void testDayDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            DataController.DAY_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.links", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.links[0]", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.links[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].measuredLocalTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.links[0].measuredUtc", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.links[0].linkMeasurements", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0]", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0].fluencyClass", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0].minute", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0].averageSpeed", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0].medianTravelTime", Matchers.notNullValue()))
                ;
    }

    @Test
    public void testDayDataRestApiById() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                MetadataApplicationConfiguration.API_DATA_PART_PATH +
                DataController.DAY_DATA_PATH + "/4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.links", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.links[0]", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.links[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].measuredLocalTime", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.links[0].measuredUtc", Matchers.isA(String.class)))
                .andExpect(jsonPath("$.links[0].linkMeasurements", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0]", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0].fluencyClass", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0].minute", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0].averageSpeed", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0].medianTravelTime", Matchers.notNullValue()))
        ;
    }

}
