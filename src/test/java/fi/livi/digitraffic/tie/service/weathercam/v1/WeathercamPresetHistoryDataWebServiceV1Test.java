package fi.livi.digitraffic.tie.service.weathercam.v1;

import static fi.livi.digitraffic.tie.helper.AssertHelper.assertCollectionSize;
import static fi.livi.digitraffic.tie.helper.AssertHelper.assertTimesEqual;
import static fi.livi.digitraffic.common.util.TimeUtil.nowWithoutMillis;
import static fi.livi.digitraffic.common.util.TimeUtil.toZonedDateTimeAtUtc;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractDaemonTest;
import fi.livi.digitraffic.tie.TestUtils;
import fi.livi.digitraffic.tie.dao.weathercam.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.dto.weathercam.v1.WeathercamStationsPresetsPublicityHistoryV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.history.WeathercamPresetHistoryDtoV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.history.WeathercamPresetHistoryItemDtoV1;
import fi.livi.digitraffic.tie.dto.weathercam.v1.history.WeathercamPresetsHistoryDtoV1;
import fi.livi.digitraffic.tie.model.weathercam.CameraPreset;
import fi.livi.digitraffic.tie.model.weathercam.CameraPresetHistory;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetHistoryUpdateService;
import fi.livi.digitraffic.tie.service.weathercam.CameraPresetService;

public class WeathercamPresetHistoryDataWebServiceV1Test extends AbstractDaemonTest {

    private static final Logger log = LoggerFactory.getLogger(WeathercamPresetHistoryDataWebServiceV1Test.class);

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraPresetHistoryRepository cameraPresetHistoryRepository;

    @Autowired
    private CameraPresetHistoryUpdateService cameraPresetHistoryUpdateService;

    @Autowired
    private WeathercamPresetHistoryDataWebServiceV1 weathercamPresetHistoryDataWebServiceV1;

    @BeforeEach
    public void initData() {
        cameraPresetHistoryRepository.deleteAll();
        TestUtils.generateDummyCameraStations(4,2)
            .forEach(camera -> camera.forEach(preset -> entityManager.persist(preset)));
        entityManager.flush();
    }

    @AfterEach
    public void clearData() {
        TestUtils.commitAndEndTransactionAndStartNew();
        TestUtils.truncateCameraData(entityManager);
        TestUtils.commitAndEndTransactionAndStartNew(); // make sure db is cleaned
    }

    @Test
    public void findCameraOrPresetPublicHistory() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        assertTrue(cp.isPresent());

        final CameraPreset preset = cp.get();
        final Instant now = nowWithoutMillis();
        final CameraPresetHistory history = generateHistory(preset, now.minus(1, ChronoUnit.MINUTES));
        cameraPresetHistoryUpdateService.saveHistory(history);

        final WeathercamPresetsHistoryDtoV1 found =
            weathercamPresetHistoryDataWebServiceV1.findCameraOrPresetPublicHistory(preset.getPresetId());
        assertNotNull(found);

        assertCollectionSize(1, found.presets);
        final WeathercamPresetHistoryDtoV1 foundPreset = found.presets.get(0);
        assertCollectionSize(1, foundPreset.history);
        final WeathercamPresetHistoryItemDtoV1 foundPresetHistory = foundPreset.history.get(0);

        assertEquals(history.getPresetId(), foundPreset.id);
        assertTrue(foundPresetHistory.imageUrl.endsWith(history.getVersionId()));
        assertEquals(history.getPresetId(), foundPreset.id);
        assertEquals(history.getLastModified().toInstant(), foundPresetHistory.lastModified);
        assertEquals(history.getSize(), foundPresetHistory.sizeBytes);
    }


    @Test
    public void findCameraOrPresetPublicHistoryIllegalIdParameter() {
        assertThrows(IllegalArgumentException.class, () ->
            weathercamPresetHistoryDataWebServiceV1.findCameraOrPresetPublicHistory("C1234"));
        assertThrows(IllegalArgumentException.class, () ->
            weathercamPresetHistoryDataWebServiceV1.findCameraOrPresetPublicHistory("C123456"));
    }

    @Test
    public void cameraOrPresetPublicHistory() {
        final int historySize = TestUtils.getRandomId(15, 27);
        final Instant lastModified = nowWithoutMillis();
        final String cameraId = generateHistoryForCamera(historySize, lastModified);
        log.info("Generated history for camera {} from {} to {} (size {})",
                 cameraId, lastModified, lastModified.minus(historySize-1, HOURS), historySize);
        // Get history for last 24 h
        final WeathercamPresetsHistoryDtoV1 history =
            weathercamPresetHistoryDataWebServiceV1.findCameraOrPresetPublicHistory(cameraId);

        assertEquals(cameraId, history.id);
        final List<WeathercamPresetHistoryDtoV1> presetsHistories = history.presets;
        assertTrue(presetsHistories.size() >= 2);

        // Every preset only once
        final Set<String> cameraPesetIds = new HashSet<>();
        presetsHistories.forEach(ph -> {
            assertFalse(cameraPesetIds.contains(ph.id));
            cameraPesetIds.add(ph.id);
        });

        // History generated for every hour and max is 24h
        final int expectedHistorySize = Math.min(24, historySize);
        // Check history size/preset and history timestamps
        presetsHistories.forEach(ph -> {
            // Generation creates history for ever hour and oldest is 24h -> max 24 stk.
            assertEquals(Math.min(historySize, 24), ph.history.size());
            IntStream.range(0, expectedHistorySize) // iterate trough history
                .forEach(i ->
                    assertEquals(lastModified.minus(i, HOURS), // expected history timestamp for every hour
                                // history order from api is from oldest to latest, so iterate it from end to start
                                 ph.history.get(expectedHistorySize-1-i).lastModified));
        });

        // Check that every url to image is unique
        final Set<String> versions = new HashSet<>();
        presetsHistories.forEach(ph -> ph.history.forEach(h -> {
            assertFalse(versions.contains(h.imageUrl));
            versions.add(h.imageUrl);
        }));

        // Check history versions size
        assertEquals(versions.size(), expectedHistorySize * presetsHistories.size());
    }

    @Test
    public void findWeathercamPresetPublicityChangesAfter() {
        final Instant start = nowWithoutMillis().minus(3, HOURS);
        final Instant change1 = start.plus(1, HOURS);
        final Instant change2 = start.plus(2, HOURS);

        final Instant lastModified1 = start;
        final Instant lastModified2 = start.plus(10, ChronoUnit.MINUTES);
        final Instant lastModified3 = start.plus(20, ChronoUnit.MINUTES);

        final CameraPreset preset = cameraPresetService.findAllPublishableCameraPresets().get(0);
        final String presetId = preset.getPresetId();

        // Generate preset history for one preset
        final CameraPresetHistory h1 = generateHistory(preset, lastModified1, true);
        final CameraPresetHistory h2 = generateHistory(preset, lastModified2, false);
        final CameraPresetHistory h3 = generateHistory(preset, lastModified3, true);
        TestUtils.updateCameraHistoryModified(presetId, h1.getVersionId(), start, entityManager);
        TestUtils.updateCameraHistoryModified(presetId, h2.getVersionId(), change1, entityManager);
        TestUtils.updateCameraHistoryModified(presetId, h3.getVersionId(), change2, entityManager);

        TestUtils.flushCommitEndTransactionAndStartNew(entityManager);

        // Changes from initial update should contain publicity changes from true -> false and false -> true
        final WeathercamStationsPresetsPublicityHistoryV1 allChangesFromStart =
            weathercamPresetHistoryDataWebServiceV1.findWeathercamPresetPublicityChangesAfter(start);
        assertCollectionSize(1, allChangesFromStart.stations);
        assertCollectionSize(2, allChangesFromStart.stations.get(0).presets);
        assertFalse(allChangesFromStart.stations.get(0).presets.get(0).publishableTo);
        assertTrue(allChangesFromStart.stations.get(0).presets.get(1).publishableTo);
        assertTimesEqual(change1, allChangesFromStart.stations.get(0).presets.get(0).modifiedTime, 0);
        assertTimesEqual(change2, allChangesFromStart.stations.get(0).presets.get(1).modifiedTime, 0);

        // Changes after first change should contain only last change from false -> true
        final WeathercamStationsPresetsPublicityHistoryV1 allChangesAfterChange1 =
            weathercamPresetHistoryDataWebServiceV1.findWeathercamPresetPublicityChangesAfter(change1);
        assertCollectionSize(1, allChangesAfterChange1.stations);
        assertCollectionSize(1, allChangesAfterChange1.stations.get(0).presets);
        assertTrue(allChangesAfterChange1.stations.get(0).presets.get(0).publishableTo);
        assertTimesEqual(change2, allChangesAfterChange1.stations.get(0).presets.get(0).modifiedTime, 0);
    }

    /**
     * Generates camera preset history for one camera. Last one timestamp will be
     * lastModified - (historySize-1) hours.
     * @param historySize How many history items to generate
     * @param lastModified Latest history item lastModified time. Others will have times with 1h decrement of previous.
     * @return Camera id
     */
    private String generateHistoryForCamera(final int historySize, final Instant lastModified) {
        final List<Map.Entry<String, List<CameraPreset>>> all =
            cameraPresetService.findAllPublishableCameraPresets().stream()
                .collect(Collectors.groupingBy(CameraPreset::getCameraId))
                .entrySet().stream().filter(e -> e.getValue().size() > 1).toList();

        // Get random camera
        final Map.Entry<String, List<CameraPreset>> camera =
            all.stream().skip((int) (all.size() * Math.random())).findAny().orElseThrow();

        camera.getValue().forEach(cameraPreset -> IntStream.range(0,historySize)
            .forEach(i -> generateHistory(cameraPreset, lastModified.minus(i, HOURS))));
        return camera.getKey();
    }

    private CameraPresetHistory generateHistory(final CameraPreset preset, final Instant lastModified) {
        return generateHistory(preset, lastModified, true);
    }
    private CameraPresetHistory generateHistory(final CameraPreset preset, final Instant lastModified, final boolean publishable) {
        final String versionId = RandomStringUtils.randomAlphanumeric(32);
        final CameraPresetHistory history =
            new CameraPresetHistory(preset.getPresetId(), versionId, preset.getId(), toZonedDateTimeAtUtc(lastModified), publishable, 10, preset.isPublic());
        cameraPresetHistoryUpdateService.saveHistory(history);
        entityManager.flush();
        return history;
    }
}
