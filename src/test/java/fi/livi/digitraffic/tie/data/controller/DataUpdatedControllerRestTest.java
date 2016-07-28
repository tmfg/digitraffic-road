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

import fi.livi.digitraffic.tie.RestTest;
import fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration;

/**
 * Test that every data-api has working last update query
 */
public class DataUpdatedControllerRestTest extends RestTest {
    private static final Logger log = LoggerFactory.getLogger(DataUpdatedControllerRestTest.class);

    @Autowired
    DataController controller;

    @Test
    public void testCameraDataRestApi() throws Exception {

        Field[] fields = FieldUtils.getAllFields(DataController.class);
        for (Field field : fields) {
            if ( field.getName().endsWith("_PATH") ) {

                String url = MetadataApplicationConfiguration.API_V1_BASE_PATH +
                             MetadataApplicationConfiguration.API_DATA_PART_PATH +
                             field.get(controller) +
                             "?" + DataController.LAST_UPDATED_PARAM + "=true";

                log.info("Test url: " + url);
                mockMvc.perform(get(url))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                        .andExpect(jsonPath("$.dataUptadedLocalTime", Matchers.notNullValue()))
                        .andExpect(jsonPath("$.dataUptadedUtc", Matchers.notNullValue()));
            }
        }
    }

}
