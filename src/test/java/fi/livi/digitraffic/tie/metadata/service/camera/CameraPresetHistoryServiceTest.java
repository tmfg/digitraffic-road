package fi.livi.digitraffic.tie.metadata.service.camera;

import static fi.livi.digitraffic.tie.helper.DateHelper.getZonedDateTimeNowAtUtc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.data.dto.camera.CameraHistoryDto;
import fi.livi.digitraffic.tie.data.dto.camera.PresetHistoryDto;
import fi.livi.digitraffic.tie.metadata.dao.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.metadata.model.CameraPreset;
import fi.livi.digitraffic.tie.metadata.model.CameraPresetHistory;
import fi.livi.digitraffic.tie.metadata.model.RoadStation;

public class CameraPresetHistoryServiceTest extends AbstractDaemonTestWithoutS3 {

    private static final Logger log = LoggerFactory.getLogger(CameraPresetHistoryServiceTest.class);

    @Autowired
    private CameraPresetService cameraPresetService;

    @Autowired
    private CameraPresetHistoryRepository cameraPresetHistoryRepository;

    @Autowired
    private CameraPresetHistoryService cameraPresetHistoryService;

    @Autowired
    private EntityManager entityManager;

    @Before
    public void cleanHistory() {
        cameraPresetHistoryRepository.deleteAll();
    }

    @Test
    public void saveHistory() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        assertTrue(cp.isPresent());

        final CameraPreset preset = cp.get();
        final ZonedDateTime now = getZonedDateTimeNowAtUtc();
        final CameraPresetHistory history = generateHistory(preset, now.minusMinutes(1));
        cameraPresetHistoryService.saveHistory(history);

        final CameraPresetHistory found = cameraPresetHistoryService.findHistoryInclSecret(preset.getPresetId(), history.getVersionId());
        assertNotNull(found);
        assertFalse("Can't be same instance to test", history.equals(found));
        assertEquals(history.getPresetId(), found.getPresetId());
        assertEquals(history.getVersionId(), found.getVersionId());
        assertEquals(history.getCameraPresetId(), found.getCameraPresetId());
        assertEquals(history.getLastModified(), found.getLastModified());
        assertEquals(history.getSize(), found.getSize());
        assertEquals(history.getCreated(), found.getCreated());
    }


    @Test
    public void historyVersions() {

        // Create 5 history item for 2 presets
        final List<String> presetIds = generateHistoryForPublicPresets(2, 5);

        presetIds.forEach(presetId -> {

            log.info("Check history for preset {}", presetId);
            final List<CameraPresetHistory> histories = cameraPresetHistoryService.findAllByPresetIdInclSecretAsc(presetId);
            assertEquals(5,histories.size());

            ZonedDateTime prevDate = null;
            for (CameraPresetHistory h : histories) {
                assertEquals(presetId, h.getPresetId());
                if (prevDate != null) {
                    assertTrue("Previous history date must be before next", prevDate.isBefore(h.getLastModified()));
                }
                prevDate = h.getLastModified();
            }
        });
    }

    @Test
    public void historyUpdateInPast() {
        final List<String> presetIds = generateHistoryForPublicPresets(2, 5);
        final String modifiedPresetId = presetIds.get(0);
        final String notModifiedPresetId = presetIds.get(1);

        final List<CameraPresetHistory> all = cameraPresetHistoryService.findAllByPresetIdInclSecretAsc(modifiedPresetId);
        final CameraPreset cp = cameraPresetService.findCameraPresetByPresetId(modifiedPresetId);
        final RoadStation rs = cp.getRoadStation();
        all.forEach(e -> entityManager.detach(e));

        // Public 0. and 1., secret 2., 3. and 4.
        CameraPresetHistory middle = all.get(2);
        rs.updatePublicity(true);
        rs.updatePublicity(false, middle.getLastModified()); // -> previous public

        cameraPresetHistoryService.updatePresetHistoryPublicityForCamera(rs);
        entityManager.flush();
        final List<CameraPresetHistory> allUpdated = cameraPresetHistoryService.findAllByPresetIdInclSecretAsc(modifiedPresetId);
        assertEquals(true, allUpdated.get(0).getPublishable());
        assertEquals(true, allUpdated.get(1).getPublishable());
        assertEquals(false, allUpdated.get(2).getPublishable());
        assertEquals(false, allUpdated.get(3).getPublishable());
        assertEquals(false, allUpdated.get(4).getPublishable());


        final List<CameraPresetHistory> allNotUpdated = cameraPresetHistoryService.findAllByPresetIdInclSecretAsc(notModifiedPresetId);
        assertEquals(true, allNotUpdated.get(0).getPublishable());
        assertEquals(true, allNotUpdated.get(1).getPublishable());
        assertEquals(true, allNotUpdated.get(2).getPublishable());
        assertEquals(true, allNotUpdated.get(3).getPublishable());
        assertEquals(true, allNotUpdated.get(4).getPublishable());
    }

    @Test
    public void historyUpdateInFuture() {
        final List<String> presetIds = generateHistoryForPublicPresets(1, 5);
        final String presetId = presetIds.get(0);

        final CameraPreset cp = cameraPresetService.findCameraPresetByPresetId(presetId);
        final RoadStation rs = cp.getRoadStation();
        rs.updatePublicity(true);
        rs.updatePublicity(false, getZonedDateTimeNowAtUtc().plusDays(1)); // -> previous/now public, future secret
        cameraPresetHistoryService.updatePresetHistoryPublicityForCamera(rs);
        entityManager.flush();
        final List<CameraPresetHistory> allUpdated = cameraPresetHistoryService.findAllByPresetIdInclSecretAsc(presetId);
        assertEquals(true, allUpdated.get(0).getPublishable());
        assertEquals(true, allUpdated.get(1).getPublishable());
        assertEquals(true, allUpdated.get(2).getPublishable());
        assertEquals(true, allUpdated.get(3).getPublishable());
        assertEquals(true, allUpdated.get(4).getPublishable());
    }

    @Test
    public void cameraOrPresetPublicHistory() {
        final int historySize = RandomUtils.nextInt(21, 28);
        final ZonedDateTime lastModified = getZonedDateTimeNowAtUtc();
        final String cameraId = generateHistoryForCamera(historySize, lastModified);
        log.info("Generated history for camera {} from {} to {} (size {})", cameraId, lastModified, lastModified.minusHours(historySize-1), historySize);
        // Get history for last 24 h
        List<CameraHistoryDto> allHistory = cameraPresetHistoryService.findCameraOrPresetPublicHistory(Collections.singletonList(cameraId), null);
        assertEquals(1, allHistory.size());
        CameraHistoryDto history = allHistory.get(0);
        assertEquals(cameraId, history.cameraId);
        List<PresetHistoryDto> presetsHistories = history.cameraHistory;
        assertTrue(presetsHistories.size() >= 2);

        // Every preset only once
        Set<String> cameraPesetIds = new HashSet<>();
        presetsHistories.forEach(ph -> {
            assertFalse(cameraPesetIds.contains(ph.getPresetId()));
            cameraPesetIds.add(ph.getPresetId());
        });

        final int expectedHistorySize = Math.min(24, historySize);
        // Check history size/preset and history timestamps
        presetsHistories.stream().forEach(ph -> {
            // Generation creates history for ever hour and oldest is 24h -> max 24 stk.
            if (historySize > 24) {
                assertEquals(24, ph.presetHistory.size());
            } else  {
                assertEquals(historySize, ph.presetHistory.size());
            }
            IntStream.range(0, expectedHistorySize).forEach(i ->
                assertEquals(lastModified.minusHours(i), ph.presetHistory.get(i).getLastModified()));
        });

        // Check that every url to image is unique
        Set<String> versions = new HashSet<>();
        presetsHistories.stream().forEach(ph -> ph.presetHistory.stream().forEach(h -> {
            assertFalse(versions.contains(h.getImageUrl()));
            versions.add(h.getImageUrl());
        }));
        // Check history versions size
        assertTrue(versions.size() == expectedHistorySize * presetsHistories.size());
    }

    private String generateHistoryForCamera(final int historySize, final ZonedDateTime lastModified) {
        Map.Entry<String, List<CameraPreset>> camera = cameraPresetService.findAllPublishableCameraPresets().stream()
            .collect(Collectors.groupingBy(CameraPreset::getCameraId))
            .entrySet().stream().filter(e -> e.getValue().size() > 1).findFirst().get();

        camera.getValue().stream().forEach(cameraPreset -> IntStream.range(0,historySize)
            .forEach(i -> generateHistory(cameraPreset, lastModified.minusHours(i))));
        return camera.getKey();
    }

    private List<String> generateHistoryForPublicPresets(final int presetCount, final int historyCountPerPreset) {
        final List<String> presetIds = new ArrayList<>();
        cameraPresetService.findAllPublishableCameraPresets().stream().limit(presetCount).forEach(cp -> {
            presetIds.add(cp.getPresetId());
            final ZonedDateTime lastModified = getZonedDateTimeNowAtUtc();
            IntStream.range(0, historyCountPerPreset).map(i -> historyCountPerPreset - i - 1).forEach(i -> {
                log.info("Create history nr. {} for preset {}", i, cp.getPresetId());
                generateHistory(cp, lastModified.minusMinutes(i));
            });
        });
        return presetIds;
    }

    private CameraPresetHistory generateHistory(final CameraPreset preset, final ZonedDateTime lastModified) {
        final String versionId = RandomStringUtils.randomAlphanumeric(32);
        final CameraPresetHistory history =
            new CameraPresetHistory(preset.getPresetId(), versionId, preset.getId(), lastModified, true, 10, getZonedDateTimeNowAtUtc());
        cameraPresetHistoryService.saveHistory(history);
        entityManager.flush();
        return history;
    }
}
