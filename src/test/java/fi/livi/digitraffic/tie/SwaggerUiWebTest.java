package fi.livi.digitraffic.tie;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import fi.livi.digitraffic.tie.controller.tms.TmsControllerV1;
import fi.livi.digitraffic.tie.service.BuildVersionService;

public class SwaggerUiWebTest extends AbstractRestWebTest {

    @Autowired
    private BuildVersionService versionService;

    private final MediaType restContentType = MediaType.APPLICATION_JSON;

    @Test
    public void testSwaggerHome() throws Exception {
        this.mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Swagger UI</title>")));
    }

    @Test
    public void testSwaggerRestRoadApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs/road-api"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(restContentType))
            .andExpect(jsonPath("$.openapi", is("3.0.1")))
            .andExpect(jsonPath("$.info.version", is(versionService.getAppFullVersion())))
            .andExpect(jsonPath("$.paths." + TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS, anything()));
    }

    @Test
    public void testSwaggerRestRoadApiBeta() throws Exception {
        mockMvc.perform(get("/v3/api-docs/road-api-beta"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(restContentType))
            .andExpect(jsonPath("$.openapi", is("3.0.1")))
            .andExpect(jsonPath("$.info.version", is(versionService.getAppFullVersion())))
            .andExpect(content().string(containsString(TmsControllerV1.API_TMS_BETA + "/")));
    }
}
