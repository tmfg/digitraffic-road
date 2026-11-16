package fi.livi.digitraffic.tie.controller.variablesign;

import static fi.livi.digitraffic.tie.controller.ApiConstants.API_SIGNS;
import static fi.livi.digitraffic.tie.controller.ApiConstants.API_SIGNS_CODE_DESCRIPTIONS;
import static fi.livi.digitraffic.tie.controller.ApiConstants.API_SIGNS_HISTORY;
import static fi.livi.digitraffic.tie.service.variablesign.v1.TestDataFilteringService.testDevices;
import static fi.livi.digitraffic.tie.service.variablesign.v1.TestDataFilteringService.testTimes;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.controller.ApiConstants;

public class VariableSignControllerV1Test extends AbstractRestWebTest {
    private WebTestClient.BodyContentSpec getJson(final String url) throws Exception {
        return logDebugResponse(executeGet(ApiConstants.API_VS_V1 + url).expectStatus().isOk());
    }

    private final static Instant NOW_DATETIME = Instant.now();
    private final static LocalDate NOW_DATE = LocalDate.ofInstant(NOW_DATETIME, ZoneId.of("UTC"));

    private static final String FILTERING_ID = testDevices.iterator().next();

    private void insertTestData() {
        entityManager.createNativeQuery(
            "insert into device(id,type,road_address,etrs_tm35fin_x,etrs_tm35fin_y,direction,carriageway) " +
            "values('ID1', 'NOPEUSRAJOITUS', '1 2 3',10, 20,'KASVAVA', 'NORMAALI');").executeUpdate();

        entityManager.createNativeQuery(
            "insert into device_data(device_id,display_value,additional_information,effect_date,cause,reliability) " +
                "values ('ID1','80',null,:time,null,'NORMAALI');")
            .setParameter("time", NOW_DATETIME)
            .executeUpdate();

        entityManager.createNativeQuery(
            "insert into device_data_row(device_data_id, screen, row_number, text) " +
                "values ((select id from device_data), 1, 1, 'TEST ROW');").executeUpdate();
    }

    /// creates data for testing TestDataFilteringService
    private void insertTestDataForTestData(final Instant time) {
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
            .jsonPath("type").value(Matchers.equalTo("FeatureCollection"))
            .jsonPath("features").value(Matchers.empty());
    }

    @Test
    public void allSignsReturnsLatestDataForEachDevice() throws Exception {
        insertTestData();

        getJson(API_SIGNS)
            .jsonPath("type").value(Matchers.equalTo("FeatureCollection"))
            .jsonPath("features").value(Matchers.hasSize(1))
            .jsonPath("features[0].properties.id").value(Matchers.equalTo("ID1"))
            .jsonPath("features[0].properties.displayValue").value(Matchers.equalTo("80"));
    }

    @Test
    public void allSignsIncludesDeletedDevices() throws Exception {
        insertTestData();
        entityManager.createNativeQuery("update device set deleted_date = now() where id = 'ID1'").executeUpdate();

        getJson(API_SIGNS)
            .jsonPath("features").value(Matchers.hasSize(1));
    }

    @Test
    public void allSignsExcludesDataOlderThan60Days() throws Exception {
        insertTestData();
        entityManager.createNativeQuery(
            "update device_data set effect_date = now() - interval '61 days' where device_id = 'ID1'").executeUpdate();

        getJson(API_SIGNS)
            .jsonPath("features").value(Matchers.empty());
    }

    @Test
    public void allSignsReturnsOnlyLatestRowPerDevice() throws Exception {
        insertTestData();
        // Insert a newer row for the same device
        entityManager.createNativeQuery(
            "insert into device_data(device_id,display_value,additional_information,effect_date,cause,reliability) " +
                "values ('ID1','120',null,:time,null,'NORMAALI');")
            .setParameter("time", NOW_DATETIME.plusSeconds(60))
            .executeUpdate();

        getJson(API_SIGNS)
            .jsonPath("features").value(Matchers.hasSize(1))
            .jsonPath("features[0].properties.displayValue").value(Matchers.equalTo("120"));
    }

    @Test
    public void notExists() throws Exception {
        executeGet(ApiConstants.API_VS_V1 + API_SIGNS + "/unknown")
            .expectStatus().isNotFound();
    }

    @Test
    public void deviceWithData() throws Exception {
        insertTestData();

        getJson(API_SIGNS + "/ID1")
            .jsonPath("features").value(Matchers.hasSize(1))
            .jsonPath("features[0].properties.displayValue").value(Matchers.equalTo("80"));
    }

    private WebTestClient.BodyContentSpec testFiltering(final int offset) throws Exception {
        final Instant time = testTimes.iterator().next().getStart().plus(offset, java.time.temporal.ChronoUnit.MINUTES);
        insertTestDataForTestData(time);

        return getJson(API_SIGNS + "?deviceId=" + FILTERING_ID);
    }

    private WebTestClient.BodyContentSpec testHistoryFiltering(final int offset) throws Exception {
        final Instant time = testTimes.iterator().next().getStart().plus(offset, java.time.temporal.ChronoUnit.MINUTES);
        insertTestDataForTestData(time);

        return getJson(API_SIGNS_HISTORY + "?deviceId=" + FILTERING_ID);
    }

    private WebTestClient.BodyContentSpec testRecentHistory() throws Exception {
        final Instant time = Instant.now().minus(Duration.ofHours(1));
        insertTestDataForTestData(time);

        return getJson(API_SIGNS_HISTORY + "?deviceId=" + FILTERING_ID);
    }

    @Test
    public void filteringWorksAtStart() throws Exception {
        testFiltering(0)
            .jsonPath("features").value(Matchers.hasSize(0));
    }

    @Test
    public void filteringWorksInMiddle() throws Exception {
        testFiltering(120)
            .jsonPath("features").value(Matchers.hasSize(0));
    }

    @Test
    public void noFiltering() throws Exception {
        testFiltering(-1)
            .jsonPath("features").value(Matchers.hasSize(1))
            .jsonPath("features[0].properties.displayValue").value(Matchers.equalTo("80"));
    }

    @Test
    public void historyFilteringWorksAtStart() throws Exception {
        testHistoryFiltering(0)
            .jsonPath("$").value(Matchers.hasSize(0));
    }

    @Test
    public void historyFilteringWorksAtMiddle() throws Exception {
        testHistoryFiltering(120)
            .jsonPath("$").value(Matchers.hasSize(0));
    }


    @Test
    public void historyNoFiltering() throws Exception {
        testRecentHistory()
            .jsonPath("$").value(Matchers.hasSize(1));
    }

    @Test
    public void historyNotExists() throws Exception {
        getJson(API_SIGNS_HISTORY + "?deviceId=unknown")
            .jsonPath("$").value(Matchers.empty());
    }

    @Test
    public void historyExists() throws Exception {
        insertTestData();

        getJson(API_SIGNS_HISTORY + "?deviceId=ID1")
            .jsonPath("$").value(Matchers.hasSize(1));
    }

    @Test
    public void historyExistsWrongDate() throws Exception {
        insertTestData();

        getJson(API_SIGNS_HISTORY + "?deviceId=ID1&effectiveDate=" + NOW_DATE.plusDays(1).format(DateTimeFormatter.ISO_DATE))
            .jsonPath("$").value(Matchers.empty());
    }

    @Test
    public void historyExistsCorrectDate() throws Exception {
        insertTestData();

        getJson(API_SIGNS_HISTORY + "?deviceId=ID1&effectiveDate=" + NOW_DATE.format(DateTimeFormatter.ISO_DATE))
            .jsonPath("$").value(Matchers.hasSize(1));
    }


    @Test
    public void codeDescriptionsV3() throws Exception {
        getJson(API_SIGNS_CODE_DESCRIPTIONS)
            .jsonPath("signTypes").value(Matchers.hasSize(12))
            .jsonPath("signTypes[0].description").value(Matchers.notNullValue())
            .jsonPath("signTypes[0].descriptionEn").value(Matchers.notNullValue());
    }
}
