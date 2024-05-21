package fi.livi.digitraffic.tie.controller.variablesign;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_SIGNS;
import static fi.livi.digitraffic.tie.controller.ApiConstants.API_SIGNS_CODE_DESCRIPTIONS;
import static fi.livi.digitraffic.tie.controller.ApiConstants.API_SIGNS_HISTORY;
import static fi.livi.digitraffic.tie.service.variablesign.v1.TestDataFilteringService.testDevices;
import static fi.livi.digitraffic.tie.service.variablesign.v1.TestDataFilteringService.testTimes;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.controller.ApiConstants;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class VariableSignControllerV1Test extends AbstractRestWebTest {
    private ResultActions getJson(final String url) throws Exception {
        final ResultActions response = executeGet(ApiConstants.API_VS_V1 + url);
        logDebugResponse(response);
        return response;
    }

    private static final ZonedDateTime now = ZonedDateTime.now();

    private static final String FILTERING_ID = testDevices.iterator().next();

    private void insertTestData() {
        entityManager.createNativeQuery(
            "insert into device(id,type,road_address,etrs_tm35fin_x,etrs_tm35fin_y,direction,carriageway) " +
            "values('ID1', 'NOPEUSRAJOITUS', '1 2 3',10, 20,'KASVAVA', 'NORMAALI');").executeUpdate();

        entityManager.createNativeQuery(
            "insert into device_data(device_id,display_value,additional_information,effect_date,cause,reliability) " +
                "values ('ID1','80',null,:time,null,'NORMAALI');")
            .setParameter("time", now)
            .executeUpdate();

        entityManager.createNativeQuery(
            "insert into device_data_row(device_data_id, screen, row_number, text) " +
                "values ((select id from device_data), 1, 1, 'TEST ROW');").executeUpdate();
    }

    /// creates data for testing TestDataFilteringService
    private void insertTestDataForTestData(final ZonedDateTime time) {
        entityManager.createNativeQuery(
            "insert into device(id,type,road_address,etrs_tm35fin_x,etrs_tm35fin_y,direction,carriageway) " +
                "values(:id, 'NOPEUSRAJOITUS', '1 2 3',10, 20,'KASVAVA', 'NORMAALI');")
            .setParameter("id", FILTERING_ID)
            .executeUpdate();

        entityManager.createNativeQuery(
                "insert into device_data(device_id,display_value,additional_information,effect_date,cause,reliability) " +
                    "values (:id,'80',null,:time,null,'NORMAALI');")
            .setParameter("id", FILTERING_ID)
            .setParameter("time", time)
            .executeUpdate();

        entityManager.createNativeQuery("update device_data set created = :created")
                .setParameter("created", time)
                    .executeUpdate();

        entityManager.createNativeQuery(
            "insert into device_data_row(device_data_id, screen, row_number, text) " +
                "values ((select id from device_data), 1, 1, 'TEST ROW');").executeUpdate();
    }

    @Test
    public void noData() throws Exception {
        getJson(API_SIGNS)
            .andExpect(status().isOk())
            .andExpect(jsonPath("type", Matchers.equalTo("FeatureCollection")))
            .andExpect(jsonPath("features", Matchers.empty()));
    }

    @Test
    public void notExists() throws Exception {
        getJson(API_SIGNS + "/unknown")
            .andExpect(status().isNotFound());
    }

    @Test
    public void deviceWithData() throws Exception {
        insertTestData();

        getJson(API_SIGNS + "/ID1")
            .andExpect(status().isOk())
            .andExpect(jsonPath("features", Matchers.hasSize(1)))
            .andExpect(jsonPath("features[0].properties.displayValue", Matchers.equalTo("80")));
    }

    private ResultActions testFiltering(final int offset) throws Exception {
        final ZonedDateTime time = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(testTimes.iterator().next().getStart().getMillis()),
            ZoneId.of("UTC"))
                .plusMinutes(offset);

        insertTestDataForTestData(time);

        return getJson(API_SIGNS + "?deviceId=" + FILTERING_ID);
    }

    private ResultActions testHistoryFiltering(final int offset) throws Exception {
        final ZonedDateTime time = ZonedDateTime.ofInstant(
                Instant.ofEpochMilli(testTimes.iterator().next().getStart().getMillis()),
                ZoneId.of("UTC"))
            .plusMinutes(offset);

        insertTestDataForTestData(time);

        return getJson(API_SIGNS_HISTORY + "?deviceId=" + FILTERING_ID);
    }

    @Test
    public void filteringWorksAtStart() throws Exception {
        testFiltering(0)
            .andExpect(status().isOk())
            .andExpect(jsonPath("features", Matchers.hasSize(0)));
    }

    @Test
    public void filteringWorksInMiddle() throws Exception {
        testFiltering(120)
            .andExpect(status().isOk())
            .andExpect(jsonPath("features", Matchers.hasSize(0)));
    }

    @Test
    public void noFiltering() throws Exception {
        testFiltering(-1)
            .andExpect(status().isOk())
            .andExpect(jsonPath("features", Matchers.hasSize(1)))
            .andExpect(jsonPath("features[0].properties.displayValue", Matchers.equalTo("80")));
    }

    @Test
    public void historyFilteringWorksAtStart() throws Exception {
        testHistoryFiltering(0)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }

    @Test
    public void historyFilteringWorksAtMiddle() throws Exception {
        testHistoryFiltering(120)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }


    @Test
    public void historyNoFiltering() throws Exception {
        testHistoryFiltering(-1)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(1)));
    }

    @Test
    public void historyNotExists() throws Exception {
        getJson(API_SIGNS_HISTORY + "?deviceId=unknown")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.empty()));
    }

    @Test
    public void historyExists() throws Exception {
        insertTestData();

        getJson(API_SIGNS_HISTORY + "?deviceId=ID1")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(1)));
    }

    @Test
    public void historyExistsWrongDate() throws Exception {
        insertTestData();

        getJson(API_SIGNS_HISTORY + "?deviceId=ID1&effectiveDate=" + now.toLocalDate().plusDays(1).format(DateTimeFormatter.ISO_DATE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.empty()));
    }

    @Test
    public void historyExistsCorrectDate() throws Exception {
        insertTestData();

        getJson(API_SIGNS_HISTORY + "?deviceId=ID1&effectiveDate=" + now.toLocalDate().format(DateTimeFormatter.ISO_DATE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(1)));
    }


    @Test
    public void codeDescriptionsV3() throws Exception {
        getJson(API_SIGNS_CODE_DESCRIPTIONS)
            .andExpect(status().isOk())
            .andExpect(jsonPath("signTypes", Matchers.hasSize(12)))
            .andExpect(jsonPath("signTypes[0].description", Matchers.notNullValue()))
            .andExpect(jsonPath("signTypes[0].descriptionEn", Matchers.notNullValue()))
        ;
    }
}
