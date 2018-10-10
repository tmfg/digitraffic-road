package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadApplicationConfiguration;

/**
 * Test that every data-api has working last update query
 */
public class UploadControllerRestWebTest extends AbstractRestWebTest {

    @Test
    public void testPostHarjaSeurantaDataOk() throws Exception {

        final String jsonContent = readResourceContent("classpath:harja/seuranta.json");

        mockMvc.perform(
            post(RoadApplicationConfiguration.API_V1_BASE_PATH +
                 RoadApplicationConfiguration.API_UPLOAD_PART_PATH + UploadController.HARJA_SEURANTA_TYOKONE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent)
            )
            .andExpect(status().isOk())
        ;
    }

}
