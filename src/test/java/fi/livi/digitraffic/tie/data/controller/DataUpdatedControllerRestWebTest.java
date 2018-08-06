package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;

/**
 * Test that every data-api has working last update query
 */
public class DataUpdatedControllerRestWebTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(DataUpdatedControllerRestWebTest.class);

    private static final Set<String> excludedOperations = Sets.newSet(
        "TRAFFIC_DISORDERS_DATEX2_PATH",
        "FORECAST_SECTION_WEATHER_DATA_PATH",
        "WEIGHT_RESTRICTIONS_DATEX2_PATH",
        "ROADWORKS_DATEX2_PATH"
    );

    @Autowired
    private DataController dataController;

    @Test
    public void testDataUpdatedExists() throws Exception {
       final Field[] fieldArray = FieldUtils.getAllFields(DataController.class);
       final List<Field> fields = Stream.of(fieldArray).filter(this::filter).collect(Collectors.toList());

        for(final Field field : fields) {
            final String url = RoadApplicationConfiguration.API_V1_BASE_PATH +
                         RoadApplicationConfiguration.API_DATA_PART_PATH +
                         field.get(dataController) +
                         "?" + DataController.LAST_UPDATED_PARAM + "=true";

            log.info("Test url: " + url);
            mockMvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$.dataUpdatedTime", Matchers.notNullValue()));
        }
    }

    private boolean filter(final Field f) {
        return f.getName().endsWith("_PATH") && !excludedOperations.contains(f.getName());
    }

}
