package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.data.controller.TrafficSignsController.DATA_PATH;
import static fi.livi.digitraffic.tie.data.controller.TrafficSignsController.METADATA_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration;

public class TrafficSignsControllerTest extends AbstractRestWebTest {
    private void postJson(final String fileName, final String function) throws Exception {
        postJson(fileName, function, MediaType.APPLICATION_JSON, status().isOk());
    }

    private void postJson(final String fileName, final String function, final MediaType mediaType, final ResultMatcher expectResult) throws Exception {
        final String jsonContent = readResourceContent("classpath:lotju/trafficsigns/" + fileName);

        final MockHttpServletRequestBuilder post = post(RoadWebApplicationConfiguration.API_V1_BASE_PATH +
            RoadWebApplicationConfiguration.API_TRAFFIC_SIGNS_PART_PATH + function)
            .content(jsonContent);
        if (mediaType != null) {
            post.contentType(mediaType);
        }

        mockMvc.perform(post).andExpect(expectResult);
    }

    @Test
    public void okMetadataFile() throws Exception {
        postJson("ok_metadata.json", METADATA_PATH);
    }

    @Test
    public void okMDataFile() throws Exception {
        postJson("ok_data.json", DATA_PATH);
    }
}
