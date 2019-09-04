package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.data.controller.TrafficSignsController.DATA_PATH;
import static fi.livi.digitraffic.tie.data.controller.TrafficSignsController.METADATA_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration;
import fi.livi.digitraffic.tie.data.dao.DeviceDataRepository;
import fi.livi.digitraffic.tie.data.dao.DeviceRepository;

public class TrafficSignsControllerTest extends AbstractRestWebTest {
    @Autowired
    public DeviceRepository deviceRepository;

    @Autowired
    public DeviceDataRepository deviceDataRepository;

    private void postJson(final String fileName, final String function) throws Exception {
        postJson(fileName, function, status().isOk());
    }

    private void postJson(final String fileName, final String function, final ResultMatcher expectResult) throws Exception {
        final String jsonContent = readResourceContent("classpath:lotju/trafficsigns/" + fileName);

        final MockHttpServletRequestBuilder post = post(RoadWebApplicationConfiguration.API_V1_BASE_PATH +
            RoadWebApplicationConfiguration.API_TRAFFIC_SIGNS_PART_PATH + function)
            .content(jsonContent);

        post.contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(post).andExpect(expectResult);
    }

    private void assertDeviceCountInDb(final int count) {
        Assert.assertEquals(count, deviceRepository.count());
    }

    private void assertDeviceDataCountInDb(final int count) {
        Assert.assertEquals(count, deviceDataRepository.count());
    }

    @Test
    @Rollback
    public void okMetadataFile() throws Exception {
        assertDeviceCountInDb(2);
        postJson("ok_metadata.json", METADATA_PATH);
        assertDeviceCountInDb(211);
    }

    @Test
    @Rollback
    public void brokenMetadataFile() throws Exception {
        assertDeviceCountInDb(2);
        postJson("broken_metadata.json", METADATA_PATH, status().is4xxClientError());
        assertDeviceCountInDb(2);
    }

    @Test
    @Rollback
    public void okDataFile() throws Exception {
        assertDeviceDataCountInDb(2);
        postJson("ok_data.json", DATA_PATH);
        assertDeviceDataCountInDb(3);
    }

    @Test
    @Rollback
    public void brokenDataFile() throws Exception {
        assertDeviceDataCountInDb(2);
        postJson("broken_data.json", DATA_PATH, status().is4xxClientError());
        assertDeviceDataCountInDb(2);
    }
}
