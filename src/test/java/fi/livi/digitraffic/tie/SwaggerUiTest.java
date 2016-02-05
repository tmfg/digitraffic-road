package fi.livi.digitraffic.tie;

import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.MetadataApplicationConfiguration.API_V1_BASE_PATH;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fi.livi.digitraffic.tie.service.BuildVersionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MetadataApplication.class)
@WebAppConfiguration
public class SwaggerUiTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private BuildVersionService versionService;

    private MediaType restContentType = MediaType.APPLICATION_JSON;


    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testSwaggerHome() throws Exception {
        this.mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Swagger UI</title>")));
    }

    @Test
    public void testSwaggerRestApi() throws Exception {
        // Swagger resta api: http://localhost:9010/v2/api-docs?group=metadata-api
        mockMvc.perform(get("/v2/api-docs?group=metadata-api"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(restContentType))
                .andExpect(jsonPath("$.swagger", is("2.0")))
                .andExpect(jsonPath("$.info.version", is(versionService.getAppFullVersion())))
                .andExpect(jsonPath("$.paths." + API_V1_BASE_PATH + API_METADATA_PART_PATH + "/lam-stations", anything()));
    }
}
