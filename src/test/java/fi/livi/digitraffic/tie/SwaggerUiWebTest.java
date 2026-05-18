package fi.livi.digitraffic.tie;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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
    public void testTrafficMessagesV1VsV2EnumsInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs/road-api"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(restContentType))
            // Top-level enums - V2 lowercase, V1 uppercase
            .andExpect(jsonPath("$.components.schemas.SituationTypeV2.enum", hasItems("traffic announcement", "exempted transport", "weight restriction", "road work")))
            .andExpect(jsonPath("$.components.schemas.SituationTypeV2.enum", not(hasItem("TRAFFIC_ANNOUNCEMENT"))))
            .andExpect(jsonPath("$.components.schemas.SituationTypeV1.enum", hasItems("TRAFFIC_ANNOUNCEMENT", "EXEMPTED_TRANSPORT", "WEIGHT_RESTRICTION", "ROAD_WORK")))
            .andExpect(jsonPath("$.components.schemas.AreaTypeV2.enum", hasItems("municipality", "province", "country")))
            .andExpect(jsonPath("$.components.schemas.AreaTypeV1.enum", hasItems("MUNICIPALITY", "PROVINCE", "COUNTRY")))
            // Inline enums in V2 properties
            .andExpect(jsonPath("$.components.schemas.TrafficAnnouncementPropertiesV2.properties.trafficAnnouncementType.enum", hasItems("general", "preliminary accident report", "accident report")))
            .andExpect(jsonPath("$.components.schemas.TrafficAnnouncementPropertiesV1.properties.trafficAnnouncementType.enum", hasItems("GENERAL", "PRELIMINARY_ACCIDENT_REPORT", "ACCIDENT_REPORT")))
            .andExpect(jsonPath("$.components.schemas.TrafficAnnouncementV2.properties.language.enum", hasItem("fi")))
            .andExpect(jsonPath("$.components.schemas.TrafficAnnouncementV1.properties.language.enum", hasItem("FI")))
            .andExpect(jsonPath("$.components.schemas.TrafficAnnouncementV2.properties.earlyClosing.enum", hasItems("closed", "canceled")))
            .andExpect(jsonPath("$.components.schemas.RoadAddressLocationV2.properties.direction.enum", hasItems("unknown", "pos", "neg", "both")))
            .andExpect(jsonPath("$.components.schemas.RoadWorkPhaseV2.properties.severity.enum", hasItems("low", "high", "highest")))
            .andExpect(jsonPath("$.components.schemas.RestrictionV2.properties.type.enum", hasItem("speed limit")))
            .andExpect(jsonPath("$.components.schemas.WeekdayTimePeriodV2.properties.weekday.enum", hasItems("Monday", "Tuesday", "Wednesday")))
            .andExpect(jsonPath("$.components.schemas.WorkTypeV2.properties.type.enum", hasItems("bridge", "road construction", "resurfacing")));
    }

    /*
    @Test
    public void testSwaggerRestRoadApiBeta() throws Exception {
        mockMvc.perform(get("/v3/api-docs/road-api-beta"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(restContentType))
            .andExpect(jsonPath("$.openapi", is("3.0.1")))
            .andExpect(jsonPath("$.info.version", is(versionService.getAppFullVersion())))
            .andExpect(content().string(not(containsString( "/api/"))));
            //.andExpect(content().string(containsString(TmsControllerV1.API_TMS_BETA + "/")));
    }*/
}
