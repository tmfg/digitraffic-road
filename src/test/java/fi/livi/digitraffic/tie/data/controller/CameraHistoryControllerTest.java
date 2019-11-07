package fi.livi.digitraffic.tie.data.controller;

import static fi.livi.digitraffic.tie.conf.RoadWebApplicationConfiguration.API_BETA_BASE_PATH;
import static fi.livi.digitraffic.tie.metadata.controller.BetaController.CAMERA_HISTORY_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.helper.DateHelper;
import fi.livi.digitraffic.tie.matcher.ZonedDateTimeMatcher;

// Methods are in BetaController now
public class CameraHistoryControllerTest extends AbstractRestWebTest {

    private static final Logger log = LoggerFactory.getLogger(CameraHistoryControllerTest.class);

    @Value("${weathercam.baseUrl}")
    private String weathercamBaseUrl;

    private static final int IMAGE_SIZE = 100000;

    private ResultActions getJson(final String url) throws Exception {
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(API_BETA_BASE_PATH + CAMERA_HISTORY_PATH + url);

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
        final String cameraId = presetId.substring(0,6);
        entityManager.createNativeQuery(
            "insert into camera_preset_history(preset_id, camera_id, version_id, camera_preset_id, last_modified, publishable, size, created)\n" +
            "VALUES ('" + presetId + "', '" + cameraId + "', '" + versionId + "',  31575, timestamp with time zone '" + lastModified.toInstant() + "', " + isPublic + ", " +
                IMAGE_SIZE + ", NOW())")
            .executeUpdate();
        return versionId;
    }

    private Matcher<String> matchUrl(String presetId, String versionId) {
        return Matchers.is(String.format("%s%s.jpg?versionId=%s", weathercamBaseUrl, presetId, versionId));
    }

    @Test
    public void notFoundForNotExistingPreset() throws Exception {
        getJson("/history/C0000000")
            .andExpect(status().isNotFound());
    }

    @Test
    public void emptyForTooOldHistory() throws Exception {
        final String presetId = "C0000001";
        insertTestData(presetId, ZonedDateTime.now().minusHours(25));

        getJson("/history/" + presetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(presetId.substring(0,6))))
            .andExpect(jsonPath("cameraHistory", Matchers.hasSize(0)))
        ;
    }

    @Test
    public void historyExistsWithoutSecret() throws Exception {
        final String presetId = "C0000002";
        final ZonedDateTime now = ZonedDateTime.now();
        final String versionId0 = insertTestData(presetId, now);
        insertTestData(presetId, now.minusMinutes(30), false); // this is not public
        final String versionId1 = insertTestData(presetId, now.minusHours(1));
        insertTestData(presetId, now.minusHours(25)); // This is too old
        insertTestData("C1234567", now); // This is for another preset

        getJson("/history/" + presetId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(presetId.substring(0,6))))
            .andExpect(jsonPath("cameraHistory[0].presetId", Matchers.is(presetId)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory", Matchers.hasSize(2)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].imageUrl", matchUrl(presetId, versionId0)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].sizeBytes", Matchers.is(IMAGE_SIZE)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[1].imageUrl", matchUrl(presetId, versionId1)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[1].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1))))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[1].sizeBytes", Matchers.is(IMAGE_SIZE)))
        ;
    }

    @Test
    public void historyAtGivenTimeTooOld() throws Exception {
        final String presetId = "C0000003";
        final ZonedDateTime tooOld = ZonedDateTime.now().minusHours(25);
        insertTestData(presetId, tooOld);

        getJson("/history/" + presetId + "?atTime=" +
            DateHelper.toZonedDateTimeAtUtc(ZonedDateTime.now()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(presetId.substring(0,6))))
            .andExpect(jsonPath("cameraHistory", Matchers.hasSize(0)))
        ;
    }

    @Test
    public void historyAtGivenTime() throws Exception {
        final String presetId = "C0000003";
        final ZonedDateTime now = ZonedDateTime.now();
        insertTestData(presetId, now);
        final String versionId = insertTestData(presetId, now.minusHours(1));

        getJson("/history/" + presetId + "?atTime=" +
                DateHelper.toZonedDateTimeAtUtc(now.minusHours(1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(presetId.substring(0,6))))
            .andExpect(jsonPath("cameraHistory[0].presetId", Matchers.is(presetId)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory", Matchers.hasSize(1)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].imageUrl", matchUrl(presetId, versionId)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1))))
        ;
    }

    @Test
    public void historyAtGivenTimeWithSecret() throws Exception {
        final String presetId = "C0000003";
        final ZonedDateTime now = ZonedDateTime.now();
        insertTestData(presetId, now);
        insertTestData(presetId, now.minusHours(1), false); // This is skipped as not public
        final String versionId = insertTestData(presetId, now.minusHours(2));

        getJson("/history/" + presetId + "?atTime=" +
            DateHelper.toZonedDateTimeAtUtc(now.minusSeconds(1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(presetId.substring(0,6))))
            .andExpect(jsonPath("cameraHistory[0].presetId", Matchers.is(presetId)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory", Matchers.hasSize(1)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].imageUrl", matchUrl(presetId, versionId)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now.minusHours(2))))
        ;
    }

    @Test
    public void historyForCamera() throws Exception {
        final String cameraId = "C00000";
        final String presetId1 = cameraId + "01";
        final String presetId2 = cameraId + "02";
        final ZonedDateTime now = ZonedDateTime.now();
        insertTestData(presetId1, now);
        insertTestData(presetId1, now.minusHours(1), false); // This is skipped as not public
        insertTestData(presetId1, now.minusHours(2));
        insertTestData(presetId2, now);
        insertTestData(presetId2, now.minusHours(1));
        insertTestData(presetId2, now.minusHours(2));

        // History data
        getJson("/history/" + cameraId)
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraId", Matchers.is(cameraId)))
            .andExpect(jsonPath("cameraHistory[0].presetId", Matchers.is(presetId1)))
            .andExpect(jsonPath("cameraHistory[1].presetId", Matchers.is(presetId2)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory", Matchers.hasSize(2)))
            .andExpect(jsonPath("cameraHistory[1].presetHistory", Matchers.hasSize(3)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now)))
            .andExpect(jsonPath("cameraHistory[0].presetHistory[1].lastModified", ZonedDateTimeMatcher.of(now.minusHours(2))))
            .andExpect(jsonPath("cameraHistory[1].presetHistory[0].lastModified", ZonedDateTimeMatcher.of(now)))
            .andExpect(jsonPath("cameraHistory[1].presetHistory[1].lastModified", ZonedDateTimeMatcher.of(now.minusHours(1))))
            .andExpect(jsonPath("cameraHistory[1].presetHistory[2].lastModified", ZonedDateTimeMatcher.of(now.minusHours(2))))
        ;

        // History status for camera is true at the -2h < t < now as there is one secret and one public preset
        assertHistoryStatusForCamera(cameraId, true, now.minusHours(2).plusSeconds(10), now.minusSeconds(10));

        // Public presetId1 at now
        assertHistoryStatusForCameraPreset(presetId1, true, now, now);
        // Secret presetId1 at -2h < t < now
        assertHistoryStatusForCameraPreset(presetId1, false, now.minusHours(2).plusSeconds(10), now.minusSeconds(10));
        // Public presetId1 at -2h time
        assertHistoryStatusForCameraPreset(presetId1, true, now.minusHours(2), now.minusHours(2));

        // Public presetId2 at now
        assertHistoryStatusForCameraPreset(presetId2, true, now, now);
        // Public presetId2 at -2h < t < now
        assertHistoryStatusForCameraPreset(presetId2, true, now.minusHours(2).plusSeconds(10), now.minusSeconds(10));
        // Public presetId2 at -2h time
        assertHistoryStatusForCameraPreset(presetId2, true, now.minusHours(2), now.minusHours(2));

    }

    private void assertHistoryStatusForCamera(final String cameraId,  final boolean status, final ZonedDateTime fromTime, final ZonedDateTime toTime)
        throws Exception {
        getJson("/status?cameraOrPresetId=" + cameraId + "&from=" + fromTime.toString() +"&to=" + toTime)
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraHistoryStatuses[0].cameraId", Matchers.is(cameraId)))
            .andExpect(jsonPath("cameraHistoryStatuses[0].history", Matchers.is(status)));
    }

    private void assertHistoryStatusForCameraPreset(final String presetId,  final boolean status, final ZonedDateTime fromTime, final ZonedDateTime toTime)
        throws Exception {
        getJson("/status?cameraOrPresetId=" + presetId + "&from=" + fromTime.toString() +"&to=" + toTime)
            .andExpect(status().isOk())
            .andExpect(jsonPath("cameraHistoryStatuses[0].cameraId", Matchers.is(presetId.substring(0,6))))
            .andExpect(jsonPath("cameraHistoryStatuses[0]..presetHistoryStatuses[?(@.presetId == \"" + presetId + "\")].history", Matchers.contains(status)))
        ;
    }

}
