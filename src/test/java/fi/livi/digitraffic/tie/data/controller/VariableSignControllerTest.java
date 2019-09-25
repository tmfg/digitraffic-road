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

    @Test
    public void deviceWithData() throws Exception {
        insertTestData();

        getJson(BetaController.VARIABLE_SIGNS_DATA_PATH + "/ID1")
            .andExpect(status().isOk())
            .andExpect(jsonPath("features", Matchers.hasSize(1)))
            .andExpect(jsonPath("features[0].properties.displayInformation", Matchers.equalTo("80")));
    }
}
