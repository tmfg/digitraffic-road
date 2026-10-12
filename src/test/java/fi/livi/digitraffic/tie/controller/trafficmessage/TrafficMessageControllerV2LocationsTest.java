package fi.livi.digitraffic.tie.controller.trafficmessage;

import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.API_TRAFFIC_MESSAGE_V2_LOCATIONS;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.TYPES;
import static fi.livi.digitraffic.tie.controller.trafficmessage.TrafficMessageControllerV2.VERSIONS;
import static fi.livi.digitraffic.tie.helper.DateHelperTest.ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.ResultActions;

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
        mockMvc.perform(get(API_TRAFFIC_MESSAGE_V2_LOCATIONS))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("FeatureCollection")))
            .andExpect(jsonPath("$.features[0].type", is("Feature")))
            .andExpect(jsonPath("$.features[0].id", isA(Integer.class)))
            .andExpect(jsonPath("$.features[0].properties.subtypeCode", isA(String.class)))
            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER);
    }

    @Test
    public void locationsLastUpdatedHasEmptyFeatures() throws Exception {
        mockMvc.perform(get(API_TRAFFIC_MESSAGE_V2_LOCATIONS).param("lastUpdated", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("FeatureCollection")))
            .andExpect(jsonPath("$.features", hasSize(0)))
            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER);
    }

    @Test
    public void locationById() throws Exception {
        mockMvc.perform(get(API_TRAFFIC_MESSAGE_V2_LOCATIONS + "/12187"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("Feature")))
            .andExpect(jsonPath("$.id", is(12187)));
    }

    @Test
    public void locationByIdNotFound() throws Exception {
        mockMvc.perform(get(API_TRAFFIC_MESSAGE_V2_LOCATIONS + "/-222"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void locationsUnknownVersionNotFound() throws Exception {
        mockMvc.perform(get(API_TRAFFIC_MESSAGE_V2_LOCATIONS).param("version", "not_found"))
            .andExpect(status().isNotFound());
        mockMvc.perform(get(API_TRAFFIC_MESSAGE_V2_LOCATIONS + "/12187").param("version", "not_found"))
            .andExpect(status().isNotFound());
        mockMvc.perform(get(API_TRAFFIC_MESSAGE_V2_LOCATIONS + TYPES).param("version", "not_found"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void locationVersionsWithoutMillis() throws Exception {
        mockMvc.perform(get(API_TRAFFIC_MESSAGE_V2_LOCATIONS + VERSIONS))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].version", notNullValue()))
            .andExpect(jsonPath("$[*].dataUpdatedTime", org.hamcrest.Matchers.everyItem(
                matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z"))));
    }

    @Test
    public void locationTypes() throws Exception {
        mockMvc.perform(get(API_TRAFFIC_MESSAGE_V2_LOCATIONS + TYPES))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.locationTypes", notNullValue()))
            .andExpect(jsonPath("$.locationSubtypes", notNullValue()))
            .andExpect(ISO_DATE_TIME_WITH_Z_AND_NO_OFFSET_CONTAINS_RESULT_MATCHER);
    }

    @Test
    public void areaGeometries() throws Exception {
        final ResultActions result = mockMvc.perform(get(AREA_GEOMETRIES_URL).param("lastUpdated", "false"));
        result.andExpect(status().isOk());
        Mockito.verify(regionGeometryDataServiceV1).findAreaLocationRegions(Mockito.eq(false), Mockito.eq(false), Mockito.isNull(), Mockito.isNull());
    }

    @Test
    public void areaGeometryNotFoundWithLastUpdated() throws Exception {
        Mockito.when(regionGeometryDataServiceV1.findAreaLocationRegions(Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any(), Mockito.eq(999)))
            .thenThrow(new ObjectNotFoundException("RegionGeometry", 999));
        mockMvc.perform(get(AREA_GEOMETRIES_URL + "/999").param("lastUpdated", "true"))
            .andExpect(status().isNotFound());
    }
}
