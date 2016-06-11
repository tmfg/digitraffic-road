package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.RestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

/**
 * Test material contains journeytime_medians from 25.8.2015 ~ 21.12.
 * So we adjust the end_timestamp in database to now - 12 h for this test
 * to be in 24 h range from now.
 */
public class DayDataRestTest extends RestTest {
    private long hoursDiff = 0;

    private final LocalDateTime DATE = LocalDateTime.of(2015, 8, 25, 21, 12);

    @Before
    public void alterEndTimeStamp() {
        hoursDiff = ChronoUnit.HOURS.between(DATE, LocalDateTime.now()) - 12;
        jdbcTemplate.update("update journeytime_median set end_timestamp = end_timestamp + ?/24", hoursDiff);
    }

    @After
    public void restoreEndTimestamp() {
        jdbcTemplate.update("update journeytime_median set end_timestamp = end_timestamp - ?/24", hoursDiff);
    }

    @Test
    public void testDayDataRestApi() throws Exception {
        mockMvc.perform(get(MetadataApplicationConfiguration.API_V1_BASE_PATH +
                            MetadataApplicationConfiguration.API_DATA_PART_PATH +
                            Data.DAY_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.links", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.links[0]", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.links[0].linkNumber", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0]", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0].m", Matchers.notNullValue()))
                .andExpect(jsonPath("$.links[0].linkMeasurements[0].tt", Matchers.notNullValue()))
                ;
    }
}
