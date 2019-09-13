package fi.livi.digitraffic.tie.data.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration;
import fi.livi.digitraffic.tie.metadata.controller.BetaController;

// methods are in BetaController now, but will move later
public class VariableSignControllerTest extends AbstractRestWebTest {
    private ResultActions getJson(final String url) throws Exception {
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(RoadWebApplicationConfiguration.API_BETA_BASE_PATH + url);

        get.contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(get);
    }

    @Test
    public void noData() throws Exception {
        getJson(BetaController.VARIABLE_SIGNS_DATA_PATH)
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", Matchers.equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", Matchers.empty()));
    }

    @Test
    public void notExists() throws Exception {
        getJson(BetaController.VARIABLE_SIGNS_DATA_PATH + "/unknown")
            .andExpect(status().isNotFound());
    }
}
