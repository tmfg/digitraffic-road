package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.controller.ApiPaths.API_DATA_PART_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.API_V2_BASE_PATH;
import static fi.livi.digitraffic.tie.controller.ApiPaths.CAMERA_HISTORY_PATH;
import static fi.livi.digitraffic.tie.helper.DateHelper.toZonedDateTimeAtUtc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.matcher.ZonedDateTimeMatcher;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;

public class CameraHistoryControllerTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(CameraHistoryControllerTest.class);

    @Value("${weathercam.baseUrl}")
    private String weathercamBaseUrl;

    private static final int IMAGE_SIZE = 100000;

    private List<List<CameraPreset>> cameras3Presets2;

    @BeforeEach
    public void initData() {
        TestUtils.truncateCameraData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();
        cameras3Presets2 = TestUtils.generateDummyCameraStations(3,2);
        cameras3Presets2.forEach(cameraPresets -> cameraPresets.forEach(preset -> entityManager.persist(preset)));
    }

    @AfterEach
    public void clearData() {
        TestUtils.truncateCameraData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew();
    }

    private ResultActions getJson(final String url) throws Exception {
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(API_V2_BASE_PATH + API_DATA_PART_PATH + CAMERA_HISTORY_PATH + url);

        get.contentType(MediaType.APPLICATION_JSON);
        final ResultActions result = mockMvc.perform(get);
        log.info("JSON:\n{}", result.andReturn().getResponse().getContentAsString());
        return result;
    }

    /** @return versionId */
    private String insertTestData(final String presetId, final ZonedDateTime lastModified) {
        return insertTestData(presetId, lastModified, true);
    }

    /** @return versionId */
    private String insertTestData(final String presetId, final ZonedDateTime lastModified, final boolean isPublic) {
        final String versionId = RandomStringUtils.randomAlphanumeric(32);
        final String cameraId = getCameraIdFromPresetId(presetId);
        entityManager.createNativeQuery(
            "insert into camera_preset_history(preset_id, camera_id, version_id, camera_preset_id, last_modified, publishable, size, created, preset_public)\n" +
            "VALUES ('" + presetId + "', '" + cameraId + "', '" + versionId + "',  (select id from camera_preset where preset_id = '" + presetId + "' and obsolete_date IS NULL) , timestamp with time zone '" + lastModified.toInstant() + "', " + isPublic + ", " +
                IMAGE_SIZE + ", NOW(), "+ true +")")
            .executeUpdate();
        return versionId;
    }

    private Matcher<String> matchUrl(String presetId, String versionId) {
        return Matchers.is(String.format("%s%s.jpg?versionId=%s", weathercamBaseUrl, presetId, versionId));
    }

    @Test
    public void emptyForNotExistingPreset() throws Exception {
        getJson("/history?id=C0000000")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }

    @Test
    public void emptyForTooOldHistory() throws Exception {
        final String presetId = cameras3Presets2.get(0).get(0).getPresetId();
        insertTestData(presetId, getZonedDateTimeNowAtUtcWithoutMillis().minusHours(25));

        getJson("/history?id=" + presetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }

    @Test
    public void historyExistsWithoutSecret() throws Exception {
        final String presetId = cameras3Presets2.get(0).get(0).getPresetId();
        final ZonedDateTime now = getZonedDateTimeNowAtUtcWithoutMillis();
        final String versionId0 = insertTestData(presetId, now);
        insertTestData(presetId, now.minusMinutes(30), false); // this is not public
        final String versionId1 = insertTestData(presetId, now.minusHours(1));
        insertTestData(presetId, now.minusHours(25)); // This is too old
        insertTestData(cameras3Presets2.get(0).get(1).getPresetId(), now); // This is for another preset

        getJson("/history?id=" + presetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0].cameraId", Matchers.is(getCameraIdFromPresetId(presetId))))
            .andExpect(jsonPath("[0].cameraHistory[0].presetId", Matchers.is(presetId)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory", Matchers.hasSize(2)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].imageUrl", matchUrl(presetId, versionId0)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].sizeBytes", Matchers.is(IMAGE_SIZE)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[1].imageUrl", matchUrl(presetId, versionId1)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[1].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1))))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[1].sizeBytes", Matchers.is(IMAGE_SIZE)))
        ;
    }

    @Test
    public void historyAtGivenTimeTooOld() throws Exception {
        final String presetId = cameras3Presets2.get(0).get(0).getPresetId();
        final ZonedDateTime tooOld = getZonedDateTimeNowAtUtcWithoutMillis().minusHours(25);
        insertTestData(presetId, tooOld);

        getJson("/history?id=" + presetId + "&at=" +
            getZonedDateTimeNowAtUtcWithoutMillis())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(0)))
        ;
    }

    @Test
    public void historyAtGivenTime() throws Exception {
        final String presetId = cameras3Presets2.get(0).get(0).getPresetId();
        final ZonedDateTime now = getZonedDateTimeNowAtUtcWithoutMillis();
        insertTestData(presetId, now);
        final String versionId = insertTestData(presetId, now.minusHours(1));

        getJson("/history?id=" + presetId + "&at=" +
                toZonedDateTimeAtUtc(now.minusHours(1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0].cameraId", Matchers.is(getCameraIdFromPresetId(presetId))))
            .andExpect(jsonPath("[0].cameraHistory[0].presetId", Matchers.is(presetId)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory", Matchers.hasSize(1)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].imageUrl", matchUrl(presetId, versionId)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1))))
        ;
    }

    @Test
    public void historyAtGivenTimeWithSecret() throws Exception {
        final String presetId = cameras3Presets2.get(0).get(0).getPresetId();
        final ZonedDateTime now = DateHelper.withoutMillisAtUtc(getZonedDateTimeNowAtUtcWithoutMillis());
        insertTestData(presetId, now);
        insertTestData(presetId, now.minusHours(1), false); // This is skipped as not public
        final String versionId = insertTestData(presetId, now.minusHours(2));

        getJson("/history?id=" + presetId + "&at=" +
            toZonedDateTimeAtUtc(now.minusSeconds(1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0].cameraId", Matchers.is(getCameraIdFromPresetId(presetId))))
            .andExpect(jsonPath("[0].cameraHistory[0].presetId", Matchers.is(presetId)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory", Matchers.hasSize(1)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].imageUrl", matchUrl(presetId, versionId)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now.minusHours(2))))
        ;
    }

    @Test
    public void historyForCamera() throws Exception {
        final String cameraId = cameras3Presets2.get(0).get(0).getCameraId();
        final String presetId1 = cameras3Presets2.get(0).get(0).getPresetId();
        final String presetId2 = cameras3Presets2.get(0).get(1).getPresetId();
        final ZonedDateTime now = getZonedDateTimeNowAtUtcWithoutMillis();
        insertTestData(presetId1, now);
        insertTestData(presetId1, now.minusHours(1), false); // This is skipped as not public
        insertTestData(presetId1, now.minusHours(2));
        insertTestData(presetId2, now);
        insertTestData(presetId2, now.minusHours(1));
        insertTestData(presetId2, now.minusHours(2));

        // History data
        final ResultActions result =
            getJson("/history?id=" + cameraId)
            .andExpect(status().isOk());

        result
            .andExpect(jsonPath("[0].cameraId", Matchers.is(cameraId)));
        // Presets are returned in alphabetical order form api, so check order
        if (isFirstPresetFirst(presetId1, presetId2 )) {
            result
                .andExpect(jsonPath("[0].cameraHistory[0].presetId", Matchers.is(presetId1)))
                .andExpect(jsonPath("[0].cameraHistory[1].presetId", Matchers.is(presetId2)))
                .andExpect(jsonPath("[0].cameraHistory[0].presetHistory", Matchers.hasSize(2)))
                .andExpect(jsonPath("[0].cameraHistory[1].presetHistory", Matchers.hasSize(3)))
                .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now)))
                .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[1].lastModified", ZonedDateTimeMatcher.of(now.minusHours(2))))
                .andExpect(jsonPath("[0].cameraHistory[1].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now)))
                .andExpect(jsonPath("[0].cameraHistory[1].presetHistory[1].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1))))
                .andExpect(jsonPath("[0].cameraHistory[1].presetHistory[2].lastModified", ZonedDateTimeMatcher.of(now.minusHours(2))));
        } else {
        // preset2 first
            result
                .andExpect(jsonPath("[0].cameraHistory[0].presetId", Matchers.is(presetId2)))
                .andExpect(jsonPath("[0].cameraHistory[1].presetId", Matchers.is(presetId1)))
                .andExpect(jsonPath("[0].cameraHistory[0].presetHistory", Matchers.hasSize(3)))
                .andExpect(jsonPath("[0].cameraHistory[1].presetHistory", Matchers.hasSize(2)))
                .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now)))
                .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[1].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1))))
                .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[2].lastModified", ZonedDateTimeMatcher.of(now.minusHours(2))))
                .andExpect(jsonPath("[0].cameraHistory[1].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now)))
                .andExpect(jsonPath("[0].cameraHistory[1].presetHistory[1].lastModified", ZonedDateTimeMatcher.of(now.minusHours(2))));
        }

        // History status for camera is true at the -2h < t < now as there is one secret and one public preset
        assertHistoryPresenceStatusForCamera(cameraId, true, now.minusHours(2).plusSeconds(10), now.minusSeconds(10));

        // Public presetId1 at now
        assertHistoryPresenceForCameraPreset(presetId1, true, now, now);
        // Secret presetId1 at -2h < t < now
        assertHistoryPresenceForCameraPreset(presetId1, false, now.minusHours(2).plusSeconds(10), now.minusSeconds(10));
        // Public presetId1 at -2h time
        assertHistoryPresenceForCameraPreset(presetId1, true, now.minusHours(2), now.minusHours(2));

        // Public presetId2 at now
        assertHistoryPresenceForCameraPreset(presetId2, true, now, now);
        // Public presetId2 at -2h < t < now
        assertHistoryPresenceForCameraPreset(presetId2, true, now.minusHours(2).plusSeconds(10), now.minusSeconds(10));
        // Public presetId2 at -2h time
        assertHistoryPresenceForCameraPreset(presetId2, true, now.minusHours(2), now.minusHours(2));
    }

    @Test
    public void findWithPresetId() throws Exception {
        final ZonedDateTime now = getZonedDateTimeNowAtUtcWithoutMillis();
        // 2 presets for camera 1 and 2
        final String c1P1 = cameras3Presets2.get(0).get(0).getPresetId();
        insertTestData(c1P1, now);
        final String c1P2 = cameras3Presets2.get(0).get(1).getPresetId();
        insertTestData(c1P2, now.minusHours(1));
        final String c2P1 = cameras3Presets2.get(1).get(0).getPresetId();
        insertTestData(c2P1, now.minusHours(2));
        final String c2P2 = cameras3Presets2.get(1).get(1).getPresetId();
        insertTestData(c2P2, now.minusHours(3));
        // camera 3 should not exist in restult as it is not requested
        final String c3P1 = cameras3Presets2.get(2).get(0).getPresetId();
        insertTestData(c3P1, now);

        final String c1 = getCameraIdFromPresetId(c1P1);

        // History data for camera 1 preset 1 and camera 2 all presets (2)
        getJson("/history?id=" + c1P1)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(1))) // 1 camera
            .andExpect(jsonPath("[0].cameraId", Matchers.is(c1))) // camera 1
            .andExpect(jsonPath("[0].cameraHistory", Matchers.hasSize(1))) // only one camera's preset history asked
            .andExpect(jsonPath("[0].cameraHistory[0].presetId", Matchers.is(c1P1)))
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now))) // c1P1
        ;
    }

    @Test
    public void findWithCameraId() throws Exception {
        final ZonedDateTime now = getZonedDateTimeNowAtUtcWithoutMillis();
        // 2 presets for camera 1 and 2
        final String c1P1 = cameras3Presets2.get(0).get(0).getPresetId();
        insertTestData(c1P1, now);
        final String c1P2 = cameras3Presets2.get(0).get(1).getPresetId();
        insertTestData(c1P2, now.minusHours(1));
        final String c2P1 = cameras3Presets2.get(1).get(0).getPresetId();
        insertTestData(c2P1, now.minusHours(2));
        final String c2P2 = cameras3Presets2.get(1).get(1).getPresetId();
        insertTestData(c2P2, now.minusHours(3));
        // camera 3 should not exist in restult as it is not requested
        final String c3P1 = cameras3Presets2.get(2).get(0).getPresetId();
        insertTestData(c3P1, now);

        final String c1 = getCameraIdFromPresetId(c1P1);

        // History data for camera 1 preset 1 and camera 2 all presets (2)
        // c1P2
        final ResultActions result = getJson("/history?id=" + c1)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(1))) // 1 camera
            .andExpect(jsonPath("[0].cameraId", Matchers.is(c1))) // camera 1
            .andExpect(jsonPath("[0].cameraHistory", Matchers.hasSize(2))) // camera has 2 presets history
            .andExpect(jsonPath("[0].cameraHistory[0].presetHistory", Matchers.hasSize(1)))
            .andExpect(jsonPath("[0].cameraHistory[1].presetHistory", Matchers.hasSize(1)));
        // Presets are returned in alphabetical order form api, so check order
        if (isFirstPresetFirst(c1P1, c1P2)) {
            result.andExpect(jsonPath("[0].cameraHistory[0].presetId", Matchers.is(c1P1)))
                  .andExpect(jsonPath("[0].cameraHistory[1].presetId", Matchers.is(c1P2)))
                  .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now))) // c1P1
                  .andExpect(jsonPath("[0].cameraHistory[1].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1)))); // c1P2
        } else {
            result.andExpect(jsonPath("[0].cameraHistory[0].presetId", Matchers.is(c1P2)))
                  .andExpect(jsonPath("[0].cameraHistory[1].presetId", Matchers.is(c1P1)))
                  .andExpect(jsonPath("[0].cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1)))) // c1P2
                  .andExpect(jsonPath("[0].cameraHistory[1].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now))); // c1P1
        }
    }

    private void assertHistoryPresenceStatusForCamera(final String cameraId, final boolean status, final ZonedDateTime fromTime, final ZonedDateTime toTime)
        throws Exception {
        getJson("/presences?cameraOrPresetId=" + cameraId + "&from=" + fromTime.toString() +"&to=" + toTime)
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraHistoryPresences[0].cameraId", Matchers.is(cameraId)))
            .andExpect(jsonPath("cameraHistoryPresences[0].historyPresent", Matchers.is(status)));
    }

    private void assertHistoryPresenceForCameraPreset(final String presetId, final boolean status, final ZonedDateTime fromTime, final ZonedDateTime toTime)
        throws Exception {
        log.info(fromTime.toString() + " -> " + toTime.toString());
        getJson("/presences?cameraOrPresetId=" + presetId + "&from=" + fromTime.toString() +"&to=" + toTime.toString())
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraHistoryPresences[0].cameraId", Matchers.is(getCameraIdFromPresetId(presetId))))
            .andExpect(jsonPath("cameraHistoryPresences[0]..presetHistoryPresences[?(@.presetId == \"" + presetId + "\")].historyPresent", Matchers.contains(status)))
        ;
    }

    private ZonedDateTime getZonedDateTimeNowAtUtcWithoutMillis() {
        return DateHelper.withoutMillisAtUtc(DateHelper.getZonedDateTimeNowAtUtc());
    }

    private String getCameraIdFromPresetId(String presetId) {
        return presetId.substring(0, 6);
    }

    private boolean isFirstPresetFirst(final String presetId1, final String presetId2) {
        return presetId1.compareTo(presetId2) <= 0;
    }
}
