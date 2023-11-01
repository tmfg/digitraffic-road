package fi.livi.digitraffic.tie.controller.weathercam;

import static fi.livi.digitraffic.tie.helper.DateHelper.getNowWithoutMillis;
import static fi.livi.digitraffic.tie.helper.DateHelper.getNowWithoutNanos;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
            .andExpect(status().isNotFound());
    }

    @Test()
    public void getWeathercamPresetsHistoryByIdNotFoundWeathercam() throws Exception {
        getCameraHistoryJson("C00000")
            .andExpect(status().isNotFound());
    }

    @Test
    public void getWeathercamPresetsHistoryByIdTooOldNotFound() throws Exception {
        final CameraPreset preset = cameras3Presets2.get(0).get(0);

        insertHistoryTestData(preset.getPresetId(), getNowWithoutNanos().minus(25, HOURS));

        getCameraHistoryJson(preset.getCameraId())
            .andExpect(status().isNotFound());

    }

    @Test
    public void getWeathercamPresetsHistoryByIdIsFound() throws Exception {
        final CameraPreset preset = cameras3Presets2.get(0).get(0);

        insertHistoryTestData(preset.getPresetId(), getNowWithoutNanos().minus(23, HOURS));

        getCameraHistoryJson(preset.getCameraId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.presets", Matchers.hasSize(1)))
            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, getTransactionTimestampRoundedToSeconds().toEpochMilli()));
    }


    @Test
    public void getWeathercamPresetsHistoryByIdExistsWithoutSecret() throws Exception {
        final CameraPreset preset_0 = cameras3Presets2.get(0).get(0);
        final CameraPreset preset_1 = cameras3Presets2.get(0).get(1);
        final Instant now = getNowWithoutMillis();

        // Insert 4 versions for preset1 with one not public and one too old
        insertHistoryTestData(preset_0.getPresetId(), now.minus(25, HOURS)); // This is too old
        final String versionId_0_0 = insertHistoryTestData(preset_0.getPresetId(), now.minus(1, HOURS));
        insertHistoryTestData(preset_0.getPresetId(), now.minus(30, ChronoUnit.MINUTES), false); // this is not public
        final String versionId_0_1 = insertHistoryTestData(preset_0.getPresetId(), now);
        // This history is for same camera but another preset
        final String versionId_1_0 = insertHistoryTestData(preset_1.getPresetId(), now);

        getCameraHistoryJson(preset_0.getCameraId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("id", is(preset_0.getCameraId())))
            .andExpect(jsonPath("presets[0].id", is(preset_0.getPresetId())))
            .andExpect(jsonPath("presets[0].history", Matchers.hasSize(2)))
            .andExpect(jsonPath("presets[0].history[0].imageUrl", matchUrl(preset_0.getPresetId(), versionId_0_0)))
            .andExpect(jsonPath("presets[0].history[0].lastModified", is(now.minus(1, HOURS).toString())))
            .andExpect(jsonPath("presets[0].history[0].sizeBytes", is(IMAGE_SIZE)))
            .andExpect(jsonPath("presets[0].history[1].imageUrl", matchUrl(preset_0.getPresetId(), versionId_0_1)))
            .andExpect(jsonPath("presets[0].history[1].lastModified", is(now.toString())))
            .andExpect(jsonPath("presets[0].history[1].sizeBytes", is(IMAGE_SIZE)))
            // 2nd preset has only one history item
            .andExpect(jsonPath("presets[1].id", is(preset_1.getPresetId())))
            .andExpect(jsonPath("presets[1].history", Matchers.hasSize(1)))
            .andExpect(jsonPath("presets[1].history[0].imageUrl", matchUrl(preset_1.getPresetId(), versionId_1_0)))
            .andExpect(jsonPath("presets[1].history[0].lastModified", is(now.toString())))
            .andExpect(jsonPath("presets[1].history[0].sizeBytes", is(IMAGE_SIZE)))

            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, getTransactionTimestampRoundedToSeconds().toEpochMilli()));
    }

    @Test
    public void getWeathercamPresetsHistoryByPresetIdExistsWithoutSecret() throws Exception {
        final CameraPreset preset_0 = cameras3Presets2.get(0).get(0);
        final CameraPreset preset_1 = cameras3Presets2.get(0).get(1);
        final Instant now = getNowWithoutMillis();
        // Insert 4 versions for preset1 with one not public and one too old
        insertHistoryTestData(preset_0.getPresetId(), now.minus(25, HOURS)); // This is too old
        final String versionId_0_0 = insertHistoryTestData(preset_0.getPresetId(), now.minus(1, HOURS));
        insertHistoryTestData(preset_0.getPresetId(), now.minus(30, ChronoUnit.MINUTES), false); // this is not public
        final String versionId_0_1 = insertHistoryTestData(preset_0.getPresetId(), now);
        // This history is for same camera but another preset and should not exist in the result
        insertHistoryTestData(preset_1.getPresetId(), now);

        getCameraHistoryJson(preset_0.getPresetId())
            .andExpect(status().isOk())
            .andExpect(jsonPath("id", is(preset_0.getCameraId())))
            .andExpect(jsonPath("presets", Matchers.hasSize(1)))
            .andExpect(jsonPath("presets[0].id", is(preset_0.getPresetId())))
            .andExpect(jsonPath("presets[0].history", Matchers.hasSize(2)))
            .andExpect(jsonPath("presets[0].history[0].imageUrl", matchUrl(preset_0.getPresetId(), versionId_0_0)))
            .andExpect(jsonPath("presets[0].history[0].lastModified", is(now.minus(1, HOURS).toString())))
            .andExpect(jsonPath("presets[0].history[0].sizeBytes", is(IMAGE_SIZE)))
            .andExpect(jsonPath("presets[0].history[1].imageUrl", matchUrl(preset_0.getPresetId(), versionId_0_1)))
            .andExpect(jsonPath("presets[0].history[1].lastModified", is(now.toString())))
            .andExpect(jsonPath("presets[0].history[1].sizeBytes", is(IMAGE_SIZE)))

            .andExpect(header().exists(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER))
            .andExpect(header().dateValue(LastModifiedAppenderControllerAdvice.LAST_MODIFIED_HEADER, getTransactionTimestampRoundedToSeconds().toEpochMilli()));
    }

    private String getCameraIdFromPresetId(String presetId) {
        return presetId.substring(0, 6);
    }

    private ResultActions getCameraHistoryJson(final String presetOrCameraId) throws Exception {
        final MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get(WeathercamControllerV1.API_WEATHERCAM_V1_STATIONS + "/" + presetOrCameraId + WeathercamControllerV1.HISTORY);

        get.contentType(MediaType.APPLICATION_JSON);
        final ResultActions result = mockMvc.perform(get);
        log.info("JSON:\n{}", result.andReturn().getResponse().getContentAsString());
        return result;
    }

    /** @return versionId */
    private String insertHistoryTestData(final String presetId, final Instant lastModified) {
        return insertHistoryTestData(presetId, lastModified, true);
    }

    /** @return versionId */
    private String insertHistoryTestData(final String presetId, final Instant lastModified, final boolean isPublic) {
        final String versionId = RandomStringUtils.randomAlphanumeric(32);
        final String cameraId = getCameraIdFromPresetId(presetId);
        entityManager.createNativeQuery(
                "insert into camera_preset_history(preset_id, camera_id, version_id, camera_preset_id, last_modified, publishable, size, created, preset_public, preset_seq_prev)\n" +
                    "VALUES ('" + presetId + "', '" + cameraId + "', '" + versionId + "',  (select id from camera_preset where preset_id = '" + presetId + "' and obsolete_date IS NULL) , timestamp with time zone '" + lastModified + "', " + isPublic + ", " +
                    IMAGE_SIZE + ", NOW(), " + true + ", null)")
            .executeUpdate();
        return versionId;
    }

    private Matcher<String> matchUrl(String presetId, String versionId) {
        return is(String.format("%s%s.jpg?versionId=%s", weathercamBaseUrl, presetId, versionId));
    }
}
