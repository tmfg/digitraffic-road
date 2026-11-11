package fi.livi.digitraffic.tie.controller.weathercam;

import static fi.livi.digitraffic.common.util.TimeUtil.nowWithoutMillis;
import static fi.livi.digitraffic.common.util.TimeUtil.nowWithoutNanos;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import org.springframework.test.web.reactive.server.WebTestClient;

import fi.livi.digitraffic.tie.AbstractRestWebTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.conf.LastModifiedAppenderControllerAdvice;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;

public class WeathercamControllerV1HistoryTest extends AbstractRestWebTest {
    private static final Logger log = LoggerFactory.getLogger(WeathercamControllerV1HistoryTest.class);

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

    @Test()
    public void getWeathercamPresetsHistoryByIdNotFoundPreset() throws Exception {
        getCameraHistoryJson("C0000000")
            .expectStatus().isNotFound();
    }

    @Test()
    public void getWeathercamPresetsHistoryByIdNotFoundWeathercam() throws Exception {
        getCameraHistoryJson("C00000")
            .expectStatus().isNotFound();
    }

    @Test
    public void getWeathercamPresetsHistoryByIdTooOldNotFound() throws Exception {
        final CameraPreset preset = cameras3Presets2.getFirst().getFirst();

        insertHistoryTestData(preset.getPresetId(), nowWithoutNanos().minus(25, HOURS));

        getCameraHistoryJson(preset.getCameraId())
            .expectStatus().isNotFound();

    }

    @Test
    public void getWeathercamPresetsHistoryByIdIsFound() throws Exception {
        final CameraPreset preset = cameras3Presets2.getFirst().getFirst();

        insertHistoryTestData(preset.getPresetId(), nowWithoutNanos().minus(23, HOURS));

        logInfoResponse(getCameraHistoryJson(preset.getCameraId())
            .expectStatus().isOk()
            .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
            .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, getTransactionTimestampRoundedToSeconds().toEpochMilli()))
            .jsonPath("$.presets").value(Matchers.hasSize(1));
    }


    @Test
    public void getWeathercamPresetsHistoryByIdExistsWithoutSecret() throws Exception {
        final CameraPreset preset_0 = cameras3Presets2.getFirst().getFirst();
        final CameraPreset preset_1 = cameras3Presets2.getFirst().get(1);
        final Instant now = nowWithoutMillis();

        // Insert 4 versions for preset1 with one not public and one too old
        insertHistoryTestData(preset_0.getPresetId(), now.minus(25, HOURS)); // This is too old
        final String versionId_0_0 = insertHistoryTestData(preset_0.getPresetId(), now.minus(1, HOURS));
        insertHistoryTestData(preset_0.getPresetId(), now.minus(30, ChronoUnit.MINUTES), false); // this is not public
        final String versionId_0_1 = insertHistoryTestData(preset_0.getPresetId(), now);
        // This history is for same camera but another preset
        final String versionId_1_0 = insertHistoryTestData(preset_1.getPresetId(), now);

        logInfoResponse(getCameraHistoryJson(preset_0.getCameraId())
            .expectStatus().isOk()
            .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
            .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, getTransactionTimestampRoundedToSeconds().toEpochMilli()))
            .jsonPath("id").value(is(preset_0.getCameraId()))
            .jsonPath("presets[0].id").value(is(preset_0.getPresetId()))
            .jsonPath("presets[0].history").value(Matchers.hasSize(2))
            .jsonPath("presets[0].history[0].imageUrl").value(matchUrl(preset_0.getPresetId(), versionId_0_0))
            .jsonPath("presets[0].history[0].lastModified").value(is(now.minus(1, HOURS).toString()))
            .jsonPath("presets[0].history[0].sizeBytes").value(is(IMAGE_SIZE))
            .jsonPath("presets[0].history[1].imageUrl").value(matchUrl(preset_0.getPresetId(), versionId_0_1))
            .jsonPath("presets[0].history[1].lastModified").value(is(now.toString()))
            .jsonPath("presets[0].history[1].sizeBytes").value(is(IMAGE_SIZE))
            // 2nd preset has only one history item
            .jsonPath("presets[1].id").value(is(preset_1.getPresetId()))
            .jsonPath("presets[1].history").value(Matchers.hasSize(1))
            .jsonPath("presets[1].history[0].imageUrl").value(matchUrl(preset_1.getPresetId(), versionId_1_0))
            .jsonPath("presets[1].history[0].lastModified").value(is(now.toString()))
            .jsonPath("presets[1].history[0].sizeBytes").value(is(IMAGE_SIZE));
    }

    @Test
    public void getWeathercamPresetsHistoryByPresetIdExistsWithoutSecret() throws Exception {
        final CameraPreset preset_0 = cameras3Presets2.getFirst().getFirst();
        final CameraPreset preset_1 = cameras3Presets2.getFirst().get(1);
        final Instant now = nowWithoutMillis();
        // Insert 4 versions for preset1 with one not public and one too old
        insertHistoryTestData(preset_0.getPresetId(), now.minus(25, HOURS)); // This is too old
        final String versionId_0_0 = insertHistoryTestData(preset_0.getPresetId(), now.minus(1, HOURS));
        insertHistoryTestData(preset_0.getPresetId(), now.minus(30, ChronoUnit.MINUTES), false); // this is not public
        final String versionId_0_1 = insertHistoryTestData(preset_0.getPresetId(), now);
        // This history is for same camera but another preset and should not exist in the result
        insertHistoryTestData(preset_1.getPresetId(), now);

        logInfoResponse(getCameraHistoryJson(preset_0.getPresetId())
            .expectStatus().isOk()
            .expectHeader().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER)
            .expectHeader().valueEqualsDate(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, getTransactionTimestampRoundedToSeconds().toEpochMilli()))
            .jsonPath("id").value(is(preset_0.getCameraId()))
            .jsonPath("presets").value(Matchers.hasSize(1))
            .jsonPath("presets[0].id").value(is(preset_0.getPresetId()))
            .jsonPath("presets[0].history").value(Matchers.hasSize(2))
            .jsonPath("presets[0].history[0].imageUrl").value(matchUrl(preset_0.getPresetId(), versionId_0_0))
            .jsonPath("presets[0].history[0].lastModified").value(is(now.minus(1, HOURS).toString()))
            .jsonPath("presets[0].history[0].sizeBytes").value(is(IMAGE_SIZE))
            .jsonPath("presets[0].history[1].imageUrl").value(matchUrl(preset_0.getPresetId(), versionId_0_1))
            .jsonPath("presets[0].history[1].lastModified").value(is(now.toString()))
            .jsonPath("presets[0].history[1].sizeBytes").value(is(IMAGE_SIZE));
    }

    private String getCameraIdFromPresetId(final String presetId) {
        return presetId.substring(0, 6);
    }

    private WebTestClient.ResponseSpec getCameraHistoryJson(final String presetOrCameraId) throws Exception {
        return executeGet(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS + "/" + presetOrCameraId + WeathercamControllerV1.HISTORY);
    }

    /** @return versionId */
    private String insertHistoryTestData(final String presetId, final Instant lastModified) {
        return insertHistoryTestData(presetId, lastModified, true);
    }

    /** @return versionId */
    private String insertHistoryTestData(final String presetId, final Instant lastModified, final boolean isPublic) {
        final String versionId = RandomStringUtils.secure().nextAlphanumeric(32);
        final String cameraId = getCameraIdFromPresetId(presetId);
        entityManager.createNativeQuery(
                "insert into camera_preset_history(preset_id, camera_id, version_id, camera_preset_id, last_modified, publishable, size, created, preset_public, preset_seq_prev)\n" +
                    "VALUES ('" + presetId + "', '" + cameraId + "', '" + versionId + "',  (select id from camera_preset where preset_id = '" + presetId + "' and obsolete_date IS NULL) , timestamp with time zone '" + lastModified + "', " + isPublic + ", " +
                    IMAGE_SIZE + ", NOW(), " + true + ", null)")
            .executeUpdate();
        return versionId;
    }

    private Matcher<String> matchUrl(final String presetId, final String versionId) {
        return is(String.format("%s%s.jpg?versionId=%s", weathercamBaseUrl, presetId, versionId));
    }
}
