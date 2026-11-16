package fi.livi.digitraffic.tie.controller.trafficmessage;

import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.API_TRAFFIC_MESSAGE_V2_LOCATIONS;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.TYPES;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.VERSIONS;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import fi.livi.digitraffic.tie.AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;

/**
 * Tests for the location and area geometry endpoints of {@link TrafficMessageControllerV2}
 */
public class TrafficMessageControllerV2LocationsTest extends AbstractRestWebTestWithRegionGeometryGitAndDataServiceMock {

    private static final String AREA_GEOMETRIES_URL =
        TrafficMessageControllerV2.API_TRAFFIC_MESSAGE_V2 + TrafficMessageControllerV2.AREA_GEOMETRIES;

    @Test
    public void locations() throws Exception {
        executeGet(API_TRAFFIC_MESSAGE_V2_LOCATIONS)
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.type").value(is("FeatureCollection"))
            .jsonPath("$.features[0].type").value(is("Feature"))
            .jsonPath("$.features[0].id").value(isA(Integer.class))
            .jsonPath("$.features[0].properties.subtypeCode").value(isA(String.class))
            .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);
    }

    @Test
    public void locationsLastUpdatedHasEmptyFeatures() throws Exception {
        executeGet(API_TRAFFIC_MESSAGE_V2_LOCATIONS + "?lastUpdated=true")
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.type").value(is("FeatureCollection"))
            .jsonPath("$.features").value(hasSize(0))
            .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);
    }

    @Test
    public void locationById() throws Exception {
        executeGet(API_TRAFFIC_MESSAGE_V2_LOCATIONS + "/12187")
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.type").value(is("Feature"))
            .jsonPath("$.id").value(is(12187));
    }

    @Test
    public void locationByIdNotFound() throws Exception {
        executeGet(API_TRAFFIC_MESSAGE_V2_LOCATIONS + "/-222")
            .expectStatus().isNotFound();
    }

    @Test
    public void locationsUnknownVersionNotFound() throws Exception {
        executeGet(API_TRAFFIC_MESSAGE_V2_LOCATIONS + "?version=not_found")
            .expectStatus().isNotFound();
        executeGet(API_TRAFFIC_MESSAGE_V2_LOCATIONS + "/12187?version=not_found")
            .expectStatus().isNotFound();
        executeGet(API_TRAFFIC_MESSAGE_V2_LOCATIONS + TYPES + "?version=not_found")
            .expectStatus().isNotFound();
    }

    @Test
    public void locationVersionsWithoutMillis() throws Exception {
        executeGet(API_TRAFFIC_MESSAGE_V2_LOCATIONS + VERSIONS)
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].version").value(notNullValue())
            .jsonPath("$[*].dataUpdatedTime").value(org.hamcrest.Matchers.everyItem(
                matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")));
    }

    @Test
    public void locationTypes() throws Exception {
        executeGet(API_TRAFFIC_MESSAGE_V2_LOCATIONS + TYPES)
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.locationTypes").value(notNullValue())
            .jsonPath("$.locationSubtypes").value(notNullValue())
            .consumeWith(this::assertBodyTimesFormatMatchesIsoDateTimeWithZ);
    }

    @Test
    public void areaGeometries() throws Exception {
        executeGet(AREA_GEOMETRIES_URL + "?lastUpdated=false").expectStatus().isOk();
        Mockito.verify(regionGeometryDataServiceV1).findAreaLocationRegions(Mockito.eq(false), Mockito.eq(false), Mockito.isNull(), Mockito.isNull());
    }

    @Test
    public void areaGeometryNotFoundWithLastUpdated() throws Exception {
        Mockito.when(regionGeometryDataServiceV1.findAreaLocationRegions(Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any(), Mockito.eq(999)))
            .thenThrow(new ObjectNotFoundException("RegionGeometry", 999));
        executeGet(AREA_GEOMETRIES_URL + "/999?lastUpdated=true")
            .expectStatus().isNotFound();
    }
}
