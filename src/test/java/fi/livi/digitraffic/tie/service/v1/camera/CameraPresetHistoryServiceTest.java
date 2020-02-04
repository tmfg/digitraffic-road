package fi.livi.digitraffic.tie.service.v1.camera;

import static fi.livi.digitraffic.tie.helper.DateHelper.getZonedDateTimeNowAtUtc;
import static fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryService.MAX_IDS_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import fi.livi.digitraffic.tie.AbstractDaemonTestWithoutS3;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.CameraHistoryPresencesDto;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryDto;
import fi.livi.digitraffic.tie.dto.v1.camera.PresetHistoryPresenceDto;
import fi.livi.digitraffic.tie.service.ObjectNotFoundException;
import fi.livi.digitraffic.tie.dao.v1.CameraPresetHistoryRepository;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPreset;
import fi.livi.digitraffic.tie.model.v1.camera.CameraPresetHistory;
import fi.livi.digitraffic.tie.model.v1.RoadStation;
import fi.livi.digitraffic.tie.service.v1.camera.CameraPresetHistoryService.HistoryStatus;

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

    @Value("${dt.amazon.s3.weathercam.bucketName}")
    private String s3WeathercamBucketName;

    @Value("${dt.amazon.s3.weathercam.region}")
    private String s3WeathercamRegion;

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

        final CameraPresetHistory found = cameraPresetHistoryService.findHistoryVersionInclSecret(preset.getPresetId(), history.getVersionId());
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

    @Test(expected = IllegalArgumentException.class)
    public void illegalIdParameter() {
        cameraPresetHistoryService.findCameraOrPresetPublicHistory(Arrays.asList("C12345", "C1234"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooLongListOfIdParameters() {
        cameraPresetHistoryService.findCameraOrPresetPublicHistory(generateCameraIds(MAX_IDS_SIZE+1), null);
    }

    @Test
    public void maxListOfIdParameters() {
        cameraPresetHistoryService.findCameraOrPresetPublicHistory(generateCameraIds(MAX_IDS_SIZE), null);
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
        final CameraPresetHistory middle = all.get(2);
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
        final List<CameraHistoryDto> allHistory = cameraPresetHistoryService.findCameraOrPresetPublicHistory(Collections.singletonList(cameraId), null);
        assertEquals(1, allHistory.size());
        final CameraHistoryDto history = allHistory.get(0);
        assertEquals(cameraId, history.cameraId);
        final List<PresetHistoryDto> presetsHistories = history.cameraHistory;
        assertTrue(presetsHistories.size() >= 2);

        // Every preset only once
        final Set<String> cameraPesetIds = new HashSet<>();
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
        final Set<String> versions = new HashSet<>();
        presetsHistories.stream().forEach(ph -> ph.presetHistory.stream().forEach(h -> {
            assertFalse(versions.contains(h.getImageUrl()));
            versions.add(h.getImageUrl());
        }));
        // Check history versions size
        assertTrue(versions.size() == expectedHistorySize * presetsHistories.size());
    }

    @Test
    public void findCameraOrPresetHistoryPresencesNullId() {
        final List<String> presetIds = generateHistoryForPublicPresets(5, 1);
        CameraPresetHistory secret = cameraPresetHistoryService.findAllByPresetIdInclSecretAsc(presetIds.get(0)).get(0);
        secret.setPublishable(false);
        entityManager.flush();

        CameraHistoryPresencesDto allCameraPresences =
            cameraPresetHistoryService.findCameraOrPresetHistoryPresences(null, null, null);
        List<PresetHistoryPresenceDto> allPresetPresences =
            allCameraPresences.cameraHistoryPresences.stream().flatMap(c -> c.presetHistoryPresences.stream()).collect(Collectors.toList());

        // Secret can't be found
        Optional<PresetHistoryPresenceDto> secretPresence =
            allPresetPresences.stream().filter(h -> h.getPresetId().equals(secret.getPresetId())).findFirst();
        assertTrue(secretPresence.isPresent());
        assertFalse(secretPresence.get().isHistoryPresent());

        allPresetPresences.stream().filter(h -> !h.getPresetId().equals(secret.getPresetId())).forEach(h -> {
            assertTrue(h.isHistoryPresent());
        });
    }

    @Test
    public void findCameraOrPresetHistoryPresences() {
        final List<String> presetIds = generateHistoryForPublicPresets(10, 1);
        final String cameraId = presetIds.get(0).substring(0, 6);
        // Make sure there is other cameras too in history
        assertTrue(presetIds.stream().filter(id -> !id.substring(0,6).equals(cameraId)).findFirst().isPresent());

        final CameraHistoryPresencesDto cameraPresences =
            cameraPresetHistoryService.findCameraOrPresetHistoryPresences(cameraId, null, null);
        final List<PresetHistoryPresenceDto> allPresetPresences =
            cameraPresences.cameraHistoryPresences.stream().flatMap(c -> c.presetHistoryPresences.stream()).collect(Collectors.toList());

        // history should exist for camera
        assertTrue(!allPresetPresences.isEmpty());
        allPresetPresences.stream().filter(h -> h.getCameraId().equals(cameraId)).forEach(h -> {
            assertTrue(h.isHistoryPresent());
        });

        // Other cameras presences should not exist
        assertFalse(allPresetPresences.stream().filter(h -> !h.getCameraId().equals(cameraId)).findFirst().isPresent());
    }

    @Test
    public void findPresetHistoryPresences() {
        final List<String> presetIds = generateHistoryForPublicPresets(10, 2);
        final String presetId = presetIds.get(0);
        // Make sure there is other presets
        assertTrue(presetIds.stream().filter(id -> !id.equals(presetId)).findFirst().isPresent());

        final CameraHistoryPresencesDto cameraPresences =
            cameraPresetHistoryService.findCameraOrPresetHistoryPresences(presetId, null, null);
        final List<PresetHistoryPresenceDto> presetPresences =
            cameraPresences.cameraHistoryPresences.stream().flatMap(c -> c.presetHistoryPresences.stream()).collect(Collectors.toList());

        // history should exist only for given preset
        assertEquals(1, presetPresences.size());
        assertTrue(presetPresences.get(0).getPresetId().equals(presetId));
        assertTrue(presetPresences.get(0).isHistoryPresent());
    }

    @Test
    public void findCameraPresetHistoryPresenceTimeLimit() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        assertTrue(cp.isPresent());
        final CameraPreset preset = cp.get();
        final ZonedDateTime now = getZonedDateTimeNowAtUtc();
        final CameraPresetHistory history0 = generateHistory(preset, now);
        final CameraPresetHistory history1 = generateHistory(preset, now.minusMinutes(1), false);
        final CameraPresetHistory history2 = generateHistory(preset, now.minusMinutes(2), false);
        final CameraPresetHistory history3 = generateHistory(preset, now.minusMinutes(3));
        cameraPresetHistoryService.saveHistory(history0);
        cameraPresetHistoryService.saveHistory(history1);
        cameraPresetHistoryService.saveHistory(history2);
        cameraPresetHistoryService.saveHistory(history3);
        entityManager.flush();

        // Only secret at given time interval
        final CameraHistoryPresencesDto cameraPresences1 =
            cameraPresetHistoryService.findCameraOrPresetHistoryPresences(preset.getPresetId(), now.minusMinutes(2), now.minusMinutes(1));
        assertEquals(1, cameraPresences1.cameraHistoryPresences.size());
        assertEquals(1, cameraPresences1.cameraHistoryPresences.get(0).presetHistoryPresences.size());
        assertEquals(false, cameraPresences1.cameraHistoryPresences.get(0).presetHistoryPresences.get(0).isHistoryPresent());

        // One public at given time interval
        final CameraHistoryPresencesDto cameraPresences2 =
            cameraPresetHistoryService.findCameraOrPresetHistoryPresences(preset.getPresetId(), now.minusMinutes(2), now);
        assertEquals(1, cameraPresences2.cameraHistoryPresences.size());
        assertEquals(1, cameraPresences2.cameraHistoryPresences.get(0).presetHistoryPresences.size());
        assertEquals(true, cameraPresences2.cameraHistoryPresences.get(0).presetHistoryPresences.get(0).isHistoryPresent());

        // One public at given time interval
        final CameraHistoryPresencesDto cameraPresences3 =
            cameraPresetHistoryService.findCameraOrPresetHistoryPresences(preset.getPresetId(), now.minusMinutes(3), now.minusMinutes(1));
        assertEquals(1, cameraPresences3.cameraHistoryPresences.size());
        assertEquals(1, cameraPresences3.cameraHistoryPresences.get(0).presetHistoryPresences.size());
        assertEquals(true, cameraPresences3.cameraHistoryPresences.get(0).presetHistoryPresences.get(0).isHistoryPresent());
    }

    @Test
    public void findLatestWithPresetIdIncSecret() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        assertTrue(cp.isPresent());
        final CameraPreset preset = cp.get();
        final ZonedDateTime now = getZonedDateTimeNowAtUtc();
        generateHistory(preset, now.minusMinutes(1));
        generateHistory(preset, now.minusMinutes(2), false);

        // Make sure there is other cameras too in history
        final CameraPresetHistory h1 = cameraPresetHistoryService.findLatestWithPresetIdIncSecret(preset.getPresetId());
        assertEquals(now.minusMinutes(1), h1.getLastModified());
        assertTrue(h1.getPublishable());

        generateHistory(preset, now, false);
        final CameraPresetHistory h2 = cameraPresetHistoryService.findLatestWithPresetIdIncSecret(preset.getPresetId());
        assertEquals(now, h2.getLastModified());
        assertFalse(h2.getPublishable());
    }

    @Test
    public void findAllByPresetIdInclSecretAsc() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        assertTrue(cp.isPresent());
        final CameraPreset preset = cp.get();
        final ZonedDateTime now = getZonedDateTimeNowAtUtc();
        generateHistory(preset, now, false);
        generateHistory(preset, now.minusMinutes(1));
        generateHistory(preset, now.minusMinutes(2), false);

        final List<CameraPresetHistory> history = cameraPresetHistoryService.findAllByPresetIdInclSecretAsc(preset.getPresetId());

        assertEquals(now.minusMinutes(2), history.get(0).getLastModified());
        assertFalse(history.get(0).getPublishable());

        assertEquals(now.minusMinutes(1), history.get(1).getLastModified());
        assertTrue(history.get(1).getPublishable());

        assertEquals(now, history.get(2).getLastModified());
        assertFalse(history.get(2).getPublishable());

    }

    @Test
    public void deleteAllWithPresetId() {
        final List<String> presetIds = generateHistoryForPublicPresets(10, 2);
        final String presetIdToDelete = presetIds.get(0);
        final String presetIdNotToDelete = presetIds.get(2);
        // Make sure there is other presets
        assertTrue(presetIds.stream().filter(id -> !id.equals(presetIdNotToDelete)).findFirst().isPresent());

        final CameraHistoryPresencesDto presetPresencesToDelete =
            cameraPresetHistoryService.findCameraOrPresetHistoryPresences(presetIdToDelete, null, null);
        final CameraHistoryPresencesDto presetPresencesNotToDelete =
            cameraPresetHistoryService.findCameraOrPresetHistoryPresences(presetIdNotToDelete, null, null);

        final PresetHistoryPresenceDto deleter = presetPresencesToDelete.cameraHistoryPresences.get(0).presetHistoryPresences.get(0);
        assertEquals(presetIdToDelete, deleter.getPresetId());
        assertTrue(presetIdToDelete, deleter.isHistoryPresent());

        final PresetHistoryPresenceDto notDelete = presetPresencesNotToDelete.cameraHistoryPresences.get(0).presetHistoryPresences.get(0);
        assertEquals(presetIdNotToDelete, notDelete.getPresetId());
        assertTrue(presetIdNotToDelete, notDelete.isHistoryPresent());

        cameraPresetHistoryService.deleteAllWithPresetId(presetIdToDelete);

        boolean exception = false;
        try {
            cameraPresetHistoryService.findCameraOrPresetHistoryPresences(presetIdToDelete, null, null);
        } catch (ObjectNotFoundException e) {
            exception = true;
        }
        assertTrue(exception);
        final CameraHistoryPresencesDto notDeleted =
            cameraPresetHistoryService.findCameraOrPresetHistoryPresences(presetIdNotToDelete, null, null);
        assertTrue(presetIdNotToDelete, notDeleted.cameraHistoryPresences.get(0).presetHistoryPresences.get(0).isHistoryPresent());

    }
    @Test
    public void resolveHistoryStatusForVersion() {
        final Optional<CameraPreset> cp = cameraPresetService.findAllPublishableCameraPresets().stream().findFirst();
        assertTrue(cp.isPresent());
        final CameraPreset preset = cp.get();
        final String presetId = preset.getPresetId();
        final ZonedDateTime now = getZonedDateTimeNowAtUtc();
        final CameraPresetHistory publicHistory = generateHistory(preset, now.minusMinutes(1));
        final CameraPresetHistory publicHistoryTooOld = generateHistory(preset, now.minusDays(2));
        final CameraPresetHistory secretHistory = generateHistory(preset, now, false);

        final HistoryStatus statusPublic =
            cameraPresetHistoryService.resolveHistoryStatusForVersion(presetId + ".jpg", publicHistory.getVersionId());
        Assert.assertEquals(HistoryStatus.PUBLIC, statusPublic);

        final HistoryStatus statusOld =
            cameraPresetHistoryService.resolveHistoryStatusForVersion(presetId + ".jpg", publicHistoryTooOld.getVersionId());
        Assert.assertEquals(HistoryStatus.TOO_OLD, statusOld);

        final HistoryStatus statusSecret =
            cameraPresetHistoryService.resolveHistoryStatusForVersion(presetId + ".jpg", secretHistory.getVersionId());
        Assert.assertEquals(HistoryStatus.SECRET, statusSecret);

        final HistoryStatus statusNotFound =
            cameraPresetHistoryService.resolveHistoryStatusForVersion(presetId + ".jpg", "FooBar123");
        Assert.assertEquals(HistoryStatus.NOT_FOUND, statusNotFound);

        final HistoryStatus statusIllegalKey =
            cameraPresetHistoryService.resolveHistoryStatusForVersion(presetId.substring(0,7) + ".jpg", publicHistory.getVersionId());
        Assert.assertEquals(HistoryStatus.ILLEGAL_KEY, statusIllegalKey);
    }

    @Test
    public void createS3UriForVersion() {
        final String image = "C1234567.jpg";
        final String imageVersions = "C1234567-versions.jpg";
        final String version = "ABCDEFG123456";
        final URI url = cameraPresetHistoryService.createS3UriForVersion(image, version);
        final String expected = "http://" + s3WeathercamBucketName + ".s3-" + s3WeathercamRegion + ".amazonaws.com/" + imageVersions + "?versionId=" + version;
        Assert.assertEquals(expected, url.toString());
    }

    private String generateHistoryForCamera(final int historySize, final ZonedDateTime lastModified) {
        final Map.Entry<String, List<CameraPreset>> camera = cameraPresetService.findAllPublishableCameraPresets().stream()
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
        return generateHistory(preset, lastModified, true);
    }
    private CameraPresetHistory generateHistory(final CameraPreset preset, final ZonedDateTime lastModified, final boolean publishable) {
        final String versionId = RandomStringUtils.randomAlphanumeric(32);
        final CameraPresetHistory history =
            new CameraPresetHistory(preset.getPresetId(), versionId, preset.getId(), lastModified, publishable, 10, getZonedDateTimeNowAtUtc());
        cameraPresetHistoryService.saveHistory(history);
        entityManager.flush();
        return history;
    }

    private static List<String> generateCameraIds(final int count) {
        return IntStream.range(0, count).mapToObj(i -> String.format("C%s", StringUtils.leftPad(String.valueOf(i), 5, "0"))).collect(Collectors.toList());
    }
}
