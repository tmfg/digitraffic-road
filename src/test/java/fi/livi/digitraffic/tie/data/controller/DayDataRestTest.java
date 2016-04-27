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
 * Test material contains journeytime_medians from 25.8.2015.  So we adjust the end_timestamp in database to yesterday for this test.
 */
public class DayDataRestTest extends RestTest {
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
                            Data.DAY_DATA_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.dataLocalTime", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.dataUtc", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkDynamicData", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkDynamicData[0]", Matchers.notNullValue())) //
                .andExpect(jsonPath("$.linkDynamicData[0].linkNumber", Matchers.notNullValue()))
                .andExpect(jsonPath("$.linkDynamicData[0].linkData", Matchers.notNullValue()))
                .andExpect(jsonPath("$.linkDynamicData[0].linkData[0]", Matchers.notNullValue()))
                .andExpect(jsonPath("$.linkDynamicData[0].linkData[0].m", Matchers.notNullValue()))
                .andExpect(jsonPath("$.linkDynamicData[0].linkData[0].tt", Matchers.notNullValue()))
                ;
    }
}
