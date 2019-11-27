package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_METADATA_PART_PATH;
import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_V2_BASE_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.metadata.controller.MetadataV2Controller;

public class VariableSignControllerTest extends AbstractRestWebTest {
    private ResultActions getJson(final String url) throws Exception {
        return getJson(API_V2_BASE_PATH + API_DATA_PART_PATH, url);
    }

    private ResultActions getJson(final String basePath, final String url) throws Exception {
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get( basePath + url);

        get.contentType(MediaType.APPLICATION_JSON);

        return mockMvc.perform(get);
    }

    private void insertTestData() {
        entityManager.createNativeQuery(
            "insert into device(id,updated_date,type,road_address,etrs_tm35fin_x,etrs_tm35fin_y,direction,carriageway) " +
            "values('ID1',current_timestamp, 'NOPEUSRAJOITUS', '1 2 3',10, 20,'KASVAVA', 'NORMAALI');").executeUpdate();

        entityManager.createNativeQuery(
            "insert into device_data(created_date,device_id,display_value,additional_information,effect_date,cause,reliability) " +
                "values (current_timestamp,'ID1','80',null,current_date,null,'NORMAALI');").executeUpdate();
    }

    @Test
    public void noData() throws Exception {
        getJson(DataV2Controller.VARIABLE_SIGNS_PATH)
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", Matchers.equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", Matchers.empty()));
    }

    @Test
    public void notExists() throws Exception {
        getJson(DataV2Controller.VARIABLE_SIGNS_PATH + "/unknown")
            .andExpect(status().isNotFound());
    }

    @Test
    public void deviceWithData() throws Exception {
        insertTestData();

        getJson(DataV2Controller.VARIABLE_SIGNS_PATH + "/ID1")
            .andExpect(status().isOk())
            .andExpect(jsonPath("features", Matchers.hasSize(1)))
            .andExpect(jsonPath("features[0].properties.displayValue", Matchers.equalTo("80")));
    }

    @Test
    public void historyNotExists() throws Exception {
        getJson(DataV2Controller.VARIABLE_SIGNS_PATH + "/history/unknown")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.empty()));
    }

    @Test
    public void historyExists() throws Exception {
        insertTestData();

        getJson(DataV2Controller.VARIABLE_SIGNS_PATH + "/history/ID1")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(1)));
    }


    @Test
    public void codeDescriptions() throws Exception {
        getJson(API_V2_BASE_PATH + API_METADATA_PART_PATH, MetadataV2Controller.CODE_DESCRIPTIONS)
            .andExpect(status().isOk())
            .andExpect(jsonPath("signTypes", Matchers.hasSize(6)))
            .andExpect(jsonPath("signTypes[0].description", Matchers.notNullValue()))
            .andExpect(jsonPath("signTypes[0].descriptionEn", Matchers.notNullValue()))
            ;
    }
}
