package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

/**
 * Test that every data-api has working last update query
 */
public class DataUpdatedControllerRestWebTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(DataUpdatedControllerRestWebTest.class);

    @Autowired
    private DataController dataController;

    @Test
    public void testDataUpdatedExists() throws Exception {

        final Field[] fields = FieldUtils.getAllFields(DataController.class);
        for (final Field field : fields) {
            if ( field.getName().endsWith("_PATH")
                 && !field.getName().equals("FLUENCY_HISTORY_DATA_PATH")
                 && !field.getName().equals("TRAFFIC_DISORDERS_DATEX2_PATH")
                 && !field.getName().equals("FORECAST_SECTION_WEATHER_DATA_PATH")) {

                final String url = MetadataApplicationConfiguration.API_V1_BASE_PATH +
                             MetadataApplicationConfiguration.API_DATA_PART_PATH +
                             field.get(dataController) +
                             "?" + DataController.LAST_UPDATED_PARAM + "=true";

                log.info("Test url: " + url);
                mockMvc.perform(get(url))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                        .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()));
            }
        }
    }

}
