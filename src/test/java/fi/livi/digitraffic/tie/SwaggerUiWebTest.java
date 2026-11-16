package fi.livi.digitraffic.tie;

import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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
        executeGet("/swagger-ui/index.html").expectStatus().isOk()
                .expectBody(String.class).value(containsString("<title>Swagger UI</title>"));
    }

    @Test
    public void testSwaggerRestRoadApi() throws Exception {
        executeGet("/v3/api-docs/road-api")
            .expectStatus().isOk()
            .expectHeader().contentType(restContentType)
            .expectBody()
            .jsonPath("$.openapi").value(is("3.0.1"))
            .jsonPath("$.info.version").value(is(versionService.getAppFullVersion()))
            .jsonPath("$.paths." + TmsControllerV1.API_TMS_V1 + TmsControllerV1.STATIONS).value(anything());
    }

    @Test
    public void testTrafficMessagesV2EnumsInOpenApi() throws Exception {
        executeGet("/v3/api-docs/road-api")
            .expectStatus().isOk()
            .expectHeader().contentType(restContentType)
            .expectBody()
            // Top-level enums - V2 lowercase
            .jsonPath("$.components.schemas.SituationTypeV2.enum").value(hasItems("traffic announcement", "exempted transport", "weight restriction", "road work"))
            .jsonPath("$.components.schemas.SituationTypeV2.enum").value(not(hasItem("TRAFFIC_ANNOUNCEMENT")))
            .jsonPath("$.components.schemas.AreaTypeV2.enum").value(hasItems("municipality", "province", "country"))
            // Inline enums in V2 properties
            .jsonPath("$.components.schemas.TrafficAnnouncementPropertiesV2.properties.trafficAnnouncementType.enum").value(hasItems("general", "preliminary accident report", "accident report"))
            .jsonPath("$.components.schemas.TrafficAnnouncementV2.properties.language.enum").value(hasItem("fi"))
            .jsonPath("$.components.schemas.TrafficAnnouncementV2.properties.earlyClosing.enum").value(hasItems("closed", "canceled"))
            .jsonPath("$.components.schemas.RoadAddressLocationV2.properties.direction.enum").value(hasItems("unknown", "pos", "neg", "both"))
            .jsonPath("$.components.schemas.RoadWorkPhaseV2.properties.severity.enum").value(hasItems("low", "high", "highest"))
            .jsonPath("$.components.schemas.RestrictionV2.properties.type.enum").value(hasItem("speed limit"))
            .jsonPath("$.components.schemas.WeekdayV2.enum").value(hasItems("Monday", "Tuesday", "Wednesday"))
            .jsonPath("$.components.schemas.WorkTypeV2.properties.type.enum").value(hasItems("bridge", "road construction", "resurfacing"));
    }
}
